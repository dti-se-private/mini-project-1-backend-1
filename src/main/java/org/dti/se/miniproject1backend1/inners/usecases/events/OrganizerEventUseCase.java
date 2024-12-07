package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.entities.*;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventTicketResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventVoucherResponse;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountUnAuthorizedException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizerEventUseCase {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    BasicEventUseCase basicEventUseCase;

    @Autowired
    EventTicketRepository eventTicketRepository;

    @Autowired
    private EventTicketFieldRepository eventTicketFieldRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    private EventVoucherRepository eventVoucherRepository;

    @Autowired
    TransactionRepository transactionRepository;

    public Mono<List<RetrieveEventResponse>> retrieveEvents(String page, String size, UUID accountId) {
        return Mono
                .fromCallable(() -> accountRepository.findFirstById(accountId))
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(account -> {
                    int pageNumber = (page != null && !page.isEmpty()) ? Integer.parseInt(page) : 0;
                    int pageSize = (size != null && !size.isEmpty()) ? Integer.parseInt(size) : 10;
                    Pageable pageable = PageRequest.of(pageNumber, pageSize);

                    return eventRepository.findByAccountId(accountId, pageable)
                            .map(event -> RetrieveEventResponse.builder()
                                    .id(event.getId())
                                    .name(event.getName())
                                    .time(event.getTime())
                                    .build())
                            .collectList();
                });
    }

    public Mono<RetrieveEventResponse> getEventById(UUID eventID, UUID accountID) {
        return basicEventUseCase.retrieveEventById(eventID)
                .flatMap(event -> {
                    if (event.getOrganizerAccount().getId().equals(accountID)) {
                        return Mono.just(event);
                    } else {
                        return Mono.error(new AccountUnAuthorizedException("You are not the owner of the event."));
                    }
                });
    }

    public Mono<RetrieveEventResponse> saveOne(CreateEventRequest request, UUID accountId) {
        return Mono
                .fromCallable(() -> accountRepository.findFirstById(accountId))
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(account -> {
                    Event newEvent = Event.builder()
                            .id(UUID.randomUUID())
                            .accountId(accountId)
                            .name(request.getName())
                            .description(request.getDescription())
                            .location(request.getLocation())
                            .category(request.getCategory())
                            .time(request.getTime())
                            .bannerImageUrl(null)
                            .build();

                    return eventRepository.save(newEvent)
                            .flatMap(savedEvent -> {
                                EventTicket eventTicket = EventTicket.builder()
                                        .id(UUID.randomUUID())
                                        .eventId(savedEvent.getId())
                                        .name(null)
                                        .description(null)
                                        .slots(request.getSlots())
                                        .price(request.getPrice())
                                        .build();

                                return eventTicketRepository.save(eventTicket)
                                        .flatMap(ticket -> {
                                            // Save event ticket fields
                                            Flux<EventTicketField> savedFields = Flux.just(
                                                            "name",
                                                            "phone",
                                                            "email",
                                                            "dob"
                                                    )
                                                    .map(key -> EventTicketField.builder()
                                                            .id(UUID.randomUUID())
                                                            .eventTicketId(ticket.getId())
                                                            .key(key)
                                                            .build())
                                                    .flatMap(eventTicketFieldRepository::save);

                                            Flux<Voucher> savedVouchers = request.getVouchers() != null
                                                    ? Flux.fromIterable(request.getVouchers()) // Use fromIterable for List
                                                    .map(voucher -> Voucher.builder()
                                                            .id(UUID.randomUUID())
                                                            .code(UUID.randomUUID().toString())
                                                            .name(voucher.getName())
                                                            .description(voucher.getDescription())
                                                            .variableAmount(voucher.getVariableAmount())
                                                            .startedAt(voucher.getStartedAt())
                                                            .endedAt(voucher.getEndedAt())
                                                            .build())
                                                    .map(voucherRepository::save)
                                                    .flatMap(voucherMono -> voucherMono)
                                                    : Flux.empty();

                                            Flux<EventVoucher> savedEventVouchers = savedVouchers
                                                    .flatMap(savedVoucher -> {
                                                        EventVoucher eventVoucher = EventVoucher.builder()
                                                                .id(UUID.randomUUID())
                                                                .eventId(savedEvent.getId())
                                                                .voucherId(savedVoucher.getId())
                                                                .build();
                                                        return eventVoucherRepository.save(eventVoucher);
                                                    });

                                            return savedFields.then(Mono.just(savedEventVouchers))
                                                    .thenMany(savedEventVouchers).then(Mono.just(savedEvent))
                                                    .then(Mono.just(savedEvent));
                                        });
                            });
                })
                .flatMap(savedEvent -> basicEventUseCase.retrieveEventById(savedEvent.getId()));
    }

    public Mono<RetrieveEventResponse> updateOne(RetrieveEventResponse request) {
        return eventRepository.findById(request.getId())
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(event -> Mono.zip(
                        updateEventDetails(event, request),
                        updateEventTickets(request.getEventTickets()),
                        updateVouchers(request.getEventVouchers()),
                        eventVoucherRepository.findByEventId(event.getId()).collectList()
                ).flatMap(tuple -> handleVoucherDeletion(tuple.getT3(), tuple.getT4())
                        .then(createResponse(tuple.getT1(), tuple.getT2(), tuple.getT3()))));
    }

    private Mono<Event> updateEventDetails(Event event, RetrieveEventResponse request) {
        return Mono.fromCallable(() -> {
            event.setName(request.getName());
            event.setDescription(request.getDescription());
            event.setLocation(request.getLocation());
            event.setCategory(request.getCategory());
            event.setTime(request.getTime());
            event.setIsNew(false);
            return event;
        }).flatMap(eventRepository::save);
    }

    private Mono<List<EventTicket>> updateEventTickets(List<RetrieveEventTicketResponse> eventTickets) {
        return Flux.fromIterable(eventTickets)
                .flatMap(ticketRequest -> eventTicketRepository.findById(ticketRequest.getId())
                        .flatMap(existingTicket -> Mono.fromCallable(() -> {
                            existingTicket.setSlots(ticketRequest.getSlots());
                            existingTicket.setPrice(ticketRequest.getPrice());
                            existingTicket.setIsNew(false);
                            return existingTicket;
                        }).flatMap(eventTicketRepository::save)))
                .collectList();
    }

    private Mono<List<Voucher>> updateVouchers(List<RetrieveEventVoucherResponse> eventVouchers) {
        return Flux.fromIterable(eventVouchers)
                .flatMap(voucherRequest -> voucherRepository.findById(voucherRequest.getId())
                        .flatMap(existingVoucher -> Mono.fromCallable(() -> {
                            existingVoucher.setName(voucherRequest.getName());
                            existingVoucher.setDescription(voucherRequest.getDescription());
                            existingVoucher.setVariableAmount(voucherRequest.getVariableAmount());
                            existingVoucher.setStartedAt(voucherRequest.getStartedAt());
                            existingVoucher.setEndedAt(voucherRequest.getEndedAt());
                            if (voucherRequest.getId() == null) {
                                existingVoucher.setId(UUID.randomUUID());
                                existingVoucher.setCode(UUID.randomUUID().toString());
                            } else {
                                existingVoucher.setIsNew(false);
                            }
                            return existingVoucher;
                        }).flatMap(voucherRepository::save)))
                .collectList();
    }

    private Mono<Void> handleVoucherDeletion(List<Voucher> updatedVouchers, List<EventVoucher> existingEventVouchers) {
        return Mono.fromCallable(() -> {
            List<UUID> updatedVoucherIds = updatedVouchers.stream()
                    .map(Voucher::getId)
                    .toList();
            return existingEventVouchers.stream()
                    .filter(existingVoucher -> !updatedVoucherIds.contains(existingVoucher.getVoucherId()))
                    .toList();
        }).flatMap(vouchersToDelete -> Flux.fromIterable(vouchersToDelete)
                .flatMap(voucher -> Mono.when(
                        eventVoucherRepository.delete(voucher),
                        voucherRepository.deleteById(voucher.getVoucherId())
                ))
                .then());
    }

    private Mono<RetrieveEventResponse> createResponse(Event updatedEvent, List<EventTicket> updatedTickets, List<Voucher> updatedVouchers) {
        return transactionRepository.countByEventId(updatedEvent.getId())
                .defaultIfEmpty(0)
                .flatMap(participantCount -> Mono.fromCallable(() -> RetrieveEventResponse.builder()
                        .id(updatedEvent.getId())
                        .name(updatedEvent.getName())
                        .description(updatedEvent.getDescription())
                        .location(updatedEvent.getLocation())
                        .category(updatedEvent.getCategory())
                        .time(updatedEvent.getTime())
                        .eventTickets(updatedTickets.stream()
                                .map(ticket -> RetrieveEventTicketResponse.builder()
                                        .slots(ticket.getSlots())
                                        .price(ticket.getPrice())
                                        .build())
                                .toList())
                        .participantCount(participantCount)
                        .eventVouchers(updatedVouchers.stream()
                                .map(voucher -> RetrieveEventVoucherResponse.builder()
                                        .id(voucher.getId())
                                        .name(voucher.getName())
                                        .description(voucher.getDescription())
                                        .variableAmount(voucher.getVariableAmount())
                                        .startedAt(voucher.getStartedAt())
                                        .endedAt(voucher.getEndedAt())
                                        .build())
                                .toList())
                        .build()));
    }
}