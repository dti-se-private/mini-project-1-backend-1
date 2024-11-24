package org.dti.se.miniproject1backend1.inners.usecases;

import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.dti.se.miniproject1backend1.inners.models.entities.EventVoucher;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.EventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.VoucherResponse;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventUseCase {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    EventVoucherRepository eventVoucherRepository;

    @Autowired
    EventTicketRepository eventTicketRepository;

    @Autowired
    TransactionRepository transactionRepository;

    public Flux<EventResponse> getTop3Events() {
        return transactionRepository.findTop3EventByTransactions()
                .flatMap(transactionCount ->
                        eventRepository.findById(transactionCount.getEventId())
                                .flatMap(event ->
                                        eventTicketRepository.findByEventId(event.getId())
                                                .map(ticket ->
                                                        EventResponse.builder()
                                                                .id(event.getId())
                                                                .accountId(event.getAccountId())
                                                                .name(event.getName())
                                                                .location(event.getLocation())
                                                                .category(event.getCategory())
                                                                .time(event.getTime())
                                                                .price(ticket.getPrice())
                                                                .slots(ticket.getSlots())
                                                                .build()
                                                )
                                )
                );
    }

    public Flux<EventResponse> getAllEvents(String category) {
        Flux<Event> eventFlux;

        if ("all".equalsIgnoreCase(category) || category == null || category.isEmpty()) {
            eventFlux = eventRepository.findAll();
        } else {
            eventFlux = eventRepository.findByCategoryIgnoreCase(category);
        }

        return eventFlux
                .flatMap(event ->
                        eventTicketRepository.findByEventId(event.getId())
                                .flatMap(ticket ->
                                        eventVoucherRepository.findByEventId(event.getId())
                                                .collectList()
                                                .flatMap(eventVouchers ->
                                                        Flux.fromIterable(eventVouchers)
                                                                .map(EventVoucher::getVoucherId)
                                                                .collectList()
                                                                .flatMap(voucherIds ->
                                                                        voucherRepository.findAllById(voucherIds)
                                                                                .collectList()
                                                                                .map(vouchers -> {
                                                                                    List<VoucherResponse> voucherDTOs = vouchers.stream()
                                                                                            .map(voucher -> VoucherResponse.builder()
                                                                                                    .id(voucher.getId())
                                                                                                    .name(voucher.getName())
                                                                                                    .description(voucher.getDescription())
                                                                                                    .variableAmount(voucher.getVariableAmount())
                                                                                                    .startedAt(voucher.getStartedAt())
                                                                                                    .endedAt(voucher.getEndedAt())
                                                                                                    .build())
                                                                                            .collect(Collectors.toList());

                                                                                    return EventResponse.builder()
                                                                                            .id(event.getId())
                                                                                            .accountId(event.getAccountId())
                                                                                            .name(event.getName())
                                                                                            .description(event.getDescription())
                                                                                            .location(event.getLocation())
                                                                                            .category(event.getCategory())
                                                                                            .time(event.getTime())
                                                                                            .price(ticket.getPrice())
                                                                                            .slots(ticket.getSlots())
                                                                                            .vouchers(voucherDTOs)
                                                                                            .build();
                                                                                })
                                                                )
                                                )
                                )
                );
    }

    public Mono<EventResponse> getEventById(UUID eventID) {
        return eventRepository.findById(eventID)
                .flatMap(event ->
                        eventTicketRepository.findByEventId(event.getId())
                                .flatMap(ticket ->
                                        eventVoucherRepository.findByEventId(event.getId())
                                                .collectList()
                                                .flatMap(eventVouchers ->
                                                        Flux.fromIterable(eventVouchers)
                                                                .map(EventVoucher::getVoucherId)
                                                                .collectList()
                                                                .flatMap(voucherIds ->
                                                                        voucherRepository.findAllById(voucherIds)
                                                                                .collectList()
                                                                                .map(vouchers -> {
                                                                                    List<VoucherResponse> voucherDTOs = vouchers.stream()
                                                                                            .map(voucher -> VoucherResponse.builder()
                                                                                                    .id(voucher.getId())
                                                                                                    .name(voucher.getName())
                                                                                                    .description(voucher.getDescription())
                                                                                                    .variableAmount(voucher.getVariableAmount())
                                                                                                    .startedAt(voucher.getStartedAt())
                                                                                                    .endedAt(voucher.getEndedAt())
                                                                                                    .build())
                                                                                            .collect(Collectors.toList());

                                                                                    return EventResponse.builder()
                                                                                            .id(event.getId())
                                                                                            .accountId(event.getAccountId())
                                                                                            .name(event.getName())
                                                                                            .description(event.getDescription())
                                                                                            .location(event.getLocation())
                                                                                            .category(event.getCategory())
                                                                                            .time(event.getTime())
                                                                                            .price(ticket.getPrice())
                                                                                            .slots(ticket.getSlots())
                                                                                            .vouchers(voucherDTOs)
                                                                                            .build();
                                                                                })
                                                                )
                                                )
                                )
                );
    }
}
