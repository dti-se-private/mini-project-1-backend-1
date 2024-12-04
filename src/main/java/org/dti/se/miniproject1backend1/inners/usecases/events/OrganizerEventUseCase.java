package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.entities.*;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventTicketResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventVoucherResponse;
import org.dti.se.miniproject1backend1.inners.usecases.authentications.JwtAuthenticationUseCase;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.UnauthorizedAccessException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizerEventUseCase {
    @Autowired
    JwtAuthenticationUseCase jwtAuthenticationUseCase;

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
        return basicEventUseCase.getEventById(eventID)
                .flatMap(event -> {
                    if (event.getOrganizerAccount().getId().equals(accountID)) {
                        return Mono.just(event);
                    } else {
                        return Mono.error(new UnauthorizedAccessException("You are not the owner of the event."));
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

                                            Flux<Voucher> savedVouchers = Flux.fromStream(
                                                        Arrays.stream(request.getVouchers())
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
                                            ).flatMap(voucherMono -> voucherMono);

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
                                                    .then(Mono.just(savedEvent));
                                        });
                            });
                })
                .flatMap(savedEvent -> basicEventUseCase.getEventById(savedEvent.getId()));
    }

    public Mono<RetrieveEventResponse> updateOne(RetrieveEventResponse request) {
        return eventRepository.findById(request.getId())
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(event -> {
                    event.setName(request.getName());
                    event.setDescription(request.getDescription());
                    event.setLocation(request.getLocation());
                    event.setCategory(request.getCategory());
                    event.setTime(request.getTime());

                    Mono<Event> updatedEventMono = eventRepository.save(event);

                    Mono<List<EventTicket>> updatedTicketsMono = Flux.fromIterable(request.getEventTickets())
                            .flatMap(ticketRequest -> eventTicketRepository
                                    .findById(ticketRequest.getId())
                                    .flatMap(existingTicket -> {
                                        existingTicket.setSlots(ticketRequest.getSlots());
                                        existingTicket.setPrice(ticketRequest.getPrice());
                                        return eventTicketRepository.save(existingTicket);
                                    })
                            ).collectList();

                    Mono<List<Voucher>> updatedVouchersMono = Flux.fromIterable(request.getEventVouchers())
                            .flatMap(voucherRequest -> voucherRepository
                                            .findById(voucherRequest.getId())
                                    .flatMap(existingVoucher -> {
                                        existingVoucher.setName(voucherRequest.getName());
                                        existingVoucher.setDescription(voucherRequest.getDescription());
                                        existingVoucher.setVariableAmount(voucherRequest.getVariableAmount());
                                        existingVoucher.setStartedAt(voucherRequest.getStartedAt());
                                        existingVoucher.setEndedAt(voucherRequest.getEndedAt());
                                        return voucherRepository.save(existingVoucher);
                                    })
                            ).collectList();

                    Mono<Integer> participantCountMono = transactionRepository.countByEventId(event.getId())
                            .defaultIfEmpty(0);

                    return Mono.zip(updatedEventMono, updatedTicketsMono, updatedVouchersMono, participantCountMono)
                            .map(tuple -> {
                                Event updatedEvent = tuple.getT1();
                                List<EventTicket> updatedTickets = tuple.getT2();
                                List<Voucher> updatedVouchers = tuple.getT3();
                                Integer participantCount = tuple.getT4();

                                return RetrieveEventResponse.builder()
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
                                                .collect(Collectors.toList()))
                                        .numberOfParticipants(participantCount)
                                        .eventVouchers(updatedVouchers.stream()
                                                .map(voucher -> RetrieveEventVoucherResponse.builder()
                                                        .id(voucher.getId())
                                                        .name(voucher.getName())
                                                        .description(voucher.getDescription())
                                                        .variableAmount(voucher.getVariableAmount())
                                                        .startedAt(voucher.getStartedAt())
                                                        .endedAt(voucher.getEndedAt())
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .build();
                            });
                });
    }
}
