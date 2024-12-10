package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.entities.*;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.*;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountUnAuthorizedException;
import org.dti.se.miniproject1backend1.outers.exceptions.events.EventNotFoundException;
import org.dti.se.miniproject1backend1.outers.exceptions.events.VoucherCodeExistsException;
import org.dti.se.miniproject1backend1.outers.repositories.customs.EventCustomRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizerEventUseCase {

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
    EventCustomRepository eventCustomRepository;
    @Autowired
    private EventVoucherRepository eventVoucherRepository;

    public Mono<List<RetrieveEventResponse>> retrieveEvents(Account authenticatedAccount, Integer page, Integer size) {
        return eventCustomRepository
                .retrieveEventsByAccountId(authenticatedAccount.getId(), page, size)
                .collectList();
    }

    public Mono<RetrieveEventResponse> retrieveEventById(Account authenticatedAccount, UUID eventId) {
        return eventCustomRepository
                .retrieveEventById(eventId)
                .filter(event -> event.getOrganizerAccount().getId().equals(authenticatedAccount.getId()))
                .switchIfEmpty(Mono.error(new AccountUnAuthorizedException()));
    }

    public Mono<RetrieveEventResponse> createEvent(Account authenticatedAccount, CreateEventRequest request) {
        return Mono
                .fromCallable(() -> {
                    Event newEvent = Event
                            .builder()
                            .id(UUID.randomUUID())
                            .accountId(authenticatedAccount.getId())
                            .name(request.getName())
                            .description(request.getDescription())
                            .location(request.getLocation())
                            .category(request.getCategory())
                            .time(request.getTime())
                            .bannerImageUrl(request.getBannerImageUrl())
                            .build();

                    List<EventTicket> newEventTickets = new ArrayList<>();
                    List<EventTicketField> newEventTicketFields = new ArrayList<>();

                    for (CreateEventTicketRequest eventTicket : request.getEventTickets()) {
                        EventTicket newEventTicket = EventTicket
                                .builder()
                                .id(UUID.randomUUID())
                                .eventId(newEvent.getId())
                                .name(eventTicket.getName())
                                .description(eventTicket.getDescription())
                                .slots(eventTicket.getSlots())
                                .price(eventTicket.getPrice())
                                .build();
                        newEventTickets.add(newEventTicket);

                        for (String eventTicketField : eventTicket.getFields()) {
                            EventTicketField newEventTicketField = EventTicketField
                                    .builder()
                                    .id(UUID.randomUUID())
                                    .eventTicketId(newEventTicket.getId())
                                    .key(eventTicketField)
                                    .build();
                            newEventTicketFields.add(newEventTicketField);
                        }
                    }

                    List<Voucher> newVouchers = new ArrayList<>();
                    List<EventVoucher> newEventVouchers = new ArrayList<>();

                    for (CreateEventVoucherRequest eventVoucher : request.getEventVouchers()) {
                        Voucher newVoucher = Voucher
                                .builder()
                                .id(UUID.randomUUID())
                                .code(eventVoucher.getCode())
                                .name(eventVoucher.getName())
                                .description(eventVoucher.getDescription())
                                .variableAmount(eventVoucher.getVariableAmount())
                                .startedAt(eventVoucher.getStartedAt())
                                .endedAt(eventVoucher.getEndedAt())
                                .build();
                        newVouchers.add(newVoucher);

                        EventVoucher newEventVoucher = EventVoucher
                                .builder()
                                .id(UUID.randomUUID())
                                .eventId(newEvent.getId())
                                .voucherId(newVoucher.getId())
                                .build();
                        newEventVouchers.add(newEventVoucher);
                    }

                    Mono<Event> savedEvent = eventRepository.save(newEvent);
                    Mono<List<EventTicket>> savedEventTickets = eventTicketRepository.saveAll(newEventTickets).collectList();
                    Mono<List<EventTicketField>> savedEventTicketFields = eventTicketFieldRepository.saveAll(newEventTicketFields).collectList();
                    Mono<List<Voucher>> savedVouchers = voucherRepository
                            .saveAll(newVouchers)
                            .onErrorResume(DuplicateKeyException.class, e -> Mono.error(new VoucherCodeExistsException()))
                            .collectList();
                    Mono<List<EventVoucher>> savedEventVouchers = eventVoucherRepository.saveAll(newEventVouchers).collectList();

                    return Mono.zip(savedEvent, savedEventTickets, savedEventTicketFields, savedVouchers, savedEventVouchers);
                })
                .flatMap(value -> value)
                .flatMap(tuple -> basicEventUseCase
                        .retrieveEventById(tuple.getT1().getId())
                );
    }

    public Mono<RetrieveEventResponse> patchEvent(
            Account authenticatedAccount,
            UUID eventId,
            PatchEventRequest request
    ) {
        return eventRepository
                .findFirstById(eventId)
                .switchIfEmpty(Mono.error(new EventNotFoundException()))
                .filter(event -> event.getAccountId().equals(authenticatedAccount.getId()))
                .switchIfEmpty(Mono.error(new AccountUnAuthorizedException()))
                .flatMap(event -> {
                    Mono<Event> patchedEvent = patchEventById(eventId, request);
                    Mono<List<EventTicket>> patchedTickets = patchEventTickets(
                            request
                                    .getEventTickets()
                                    .stream()
                                    .map(PatchEventTicketRequest::getId)
                                    .toList(),
                            request
                                    .getEventTickets()
                    );
                    Mono<List<EventTicketField>> patchedTicketFields = patchEventTicketFields(
                            request
                                    .getEventTickets()
                                    .stream()
                                    .flatMap(eventTicket -> eventTicket
                                            .getFields()
                                            .stream()
                                            .map(PatchEventTicketFieldRequest::getId)
                                    )
                                    .toList(),
                            request
                                    .getEventTickets()
                                    .stream()
                                    .flatMap(eventTicket -> eventTicket
                                            .getFields()
                                            .stream()
                                    )
                                    .toList()
                    );

                    Mono<List<Voucher>> patchedVouchers = patchVouchers(
                            request.getEventVouchers()
                                    .stream()
                                    .filter(voucher -> voucher.getId() != null)
                                    .map(PatchEventVoucherRequest::getId)
                                    .toList(),
                            request.getEventVouchers()

                    );

                    Mono<List<Voucher>> createdVouchers = createVouchers(
                            eventId,
                            request.getEventVouchers()
                                    .stream()
                                    .filter(voucher -> voucher.getId() == null)
                                    .toList()
                    );

                    return Mono.zip(patchedEvent, patchedTickets, patchedTicketFields, patchedVouchers, createdVouchers);
                })
                .flatMap(tuple -> basicEventUseCase
                        .retrieveEventById(eventId)
                );
    }

    private Mono<Event> patchEventById(UUID eventId, PatchEventRequest patcherEvent) {
        return eventRepository
                .findFirstById(eventId)
                .switchIfEmpty(Mono.error(new EventNotFoundException()))
                .map(event -> event
                        .setIsNew(false)
                        .setName(patcherEvent.getName())
                        .setDescription(patcherEvent.getDescription())
                        .setLocation(patcherEvent.getLocation())
                        .setCategory(patcherEvent.getCategory())
                        .setTime(patcherEvent.getTime())
                        .setBannerImageUrl(patcherEvent.getBannerImageUrl())
                )
                .flatMap(eventRepository::save);
    }

    private Mono<List<EventTicket>> patchEventTickets(List<UUID> eventTicketIds, List<PatchEventTicketRequest> patcherEventTickets) {
        return eventTicketRepository
                .findAllById(eventTicketIds)
                .map(eventTicket -> {
                    PatchEventTicketRequest patcherEventTicket = patcherEventTickets
                            .stream()
                            .filter(patcherTicket -> patcherTicket.getId().equals(eventTicket.getId()))
                            .findFirst()
                            .orElseThrow();
                    return eventTicket
                            .setIsNew(false)
                            .setName(patcherEventTicket.getName())
                            .setDescription(patcherEventTicket.getDescription())
                            .setSlots(patcherEventTicket.getSlots())
                            .setPrice(patcherEventTicket.getPrice());
                })
                .flatMap(eventTicketRepository::save)
                .collectList();
    }

    private Mono<List<EventTicketField>> patchEventTicketFields(List<UUID> eventTicketFieldIds, List<PatchEventTicketFieldRequest> patcherEventTicketFields) {
        return eventTicketFieldRepository
                .findAllById(eventTicketFieldIds)
                .map(eventTicketField -> {
                    PatchEventTicketFieldRequest patcherEventTicketField = patcherEventTicketFields
                            .stream()
                            .filter(patcherField -> patcherField
                                    .getId()
                                    .equals(eventTicketField.getId())
                            )
                            .findFirst()
                            .orElseThrow();
                    return eventTicketField
                            .setIsNew(false)
                            .setKey(patcherEventTicketField.getKey());
                })
                .flatMap(eventTicketFieldRepository::save)
                .collectList();
    }

    private Mono<List<Voucher>> patchVouchers(List<UUID> voucherIds, List<PatchEventVoucherRequest> patcherEventVouchers) {
        return voucherRepository
                .findAllById(voucherIds)
                .map(voucher -> {
                    PatchEventVoucherRequest patcherEventVoucher = patcherEventVouchers
                            .stream()
                            .filter(patcherVoucher -> patcherVoucher.getId().equals(voucher.getId()))
                            .findFirst()
                            .orElseThrow();
                    return voucher
                            .setIsNew(false)
                            .setName(patcherEventVoucher.getName())
                            .setDescription(patcherEventVoucher.getDescription())
                            .setVariableAmount(patcherEventVoucher.getVariableAmount())
                            .setStartedAt(patcherEventVoucher.getStartedAt())
                            .setEndedAt(patcherEventVoucher.getEndedAt());
                })
                .flatMap(voucherRepository::save)
                .collectList();
    }

    private Mono<List<Voucher>> createVouchers(UUID eventId, List<PatchEventVoucherRequest> creatorEventVouchers) {
        return voucherRepository
                .saveAll(creatorEventVouchers
                        .stream()
                        .map(voucher -> Voucher
                                .builder()
                                .id(UUID.randomUUID())
                                .code(voucher.getCode())
                                .name(voucher.getName())
                                .description(voucher.getDescription())
                                .variableAmount(voucher.getVariableAmount())
                                .startedAt(voucher.getStartedAt())
                                .endedAt(voucher.getEndedAt())
                                .build()
                        )
                        .toList()
                )
                .collectList()
                .flatMap(vouchers -> eventVoucherRepository
                        .saveAll(vouchers
                                .stream()
                                .map(voucher -> EventVoucher
                                        .builder()
                                        .id(UUID.randomUUID())
                                        .eventId(eventId)
                                        .voucherId(voucher.getId())
                                        .build()
                                )
                                .toList()
                        )
                        .then(Mono.just(vouchers))
                );
    }

}