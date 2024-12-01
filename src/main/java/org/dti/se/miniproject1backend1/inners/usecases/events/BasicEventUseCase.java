package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.entities.EventVoucher;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.vouchers.RetrieveVoucherResponse;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BasicEventUseCase {

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

    @Autowired
    AccountRepository accountRepository;

    public Flux<RetrieveEventResponse> getTop3Events() {
        return transactionRepository.findTop3EventByTransactions()
                .flatMap(transactionCount -> eventRepository
                         .findById(transactionCount.getEventId())
                .flatMap(event -> eventTicketRepository.findByEventId(event.getId())
                         .map(ticket -> RetrieveEventResponse.builder()
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
                ));
    }

    public Flux<RetrieveEventResponse> getAllEvents(String category, String page, String size) {
        return Mono.fromCallable(() -> {
            int pageNumber = (page != null && !page.isEmpty()) ? Integer.parseInt(page) : 0;
            int pageSize = (size != null && !size.isEmpty()) ? Integer.parseInt(size) : 10;
            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            return ("all".equalsIgnoreCase(category) || category == null || category.isEmpty())
                    ? eventRepository.findAllBy(pageable)
                    : eventRepository.findByCategoryIgnoreCase(category, pageable);
        }).flatMapMany(eventFlux -> eventFlux.flatMap(event -> eventTicketRepository
                .findByEventId(event.getId())
                .map(ticket -> RetrieveEventResponse.builder()
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
        ));
    }

    public Mono<RetrieveEventResponse> getEventById(UUID eventID) {
        return eventRepository
                .findById(eventID)
                .flatMap(event -> accountRepository
                        .findById(event.getAccountId())
                .flatMap(account -> eventTicketRepository
                        .findByEventId(event.getId())
                .flatMap(ticket -> eventVoucherRepository
                         .findByEventId(event.getId())
                         .collectList()
                .flatMap(eventVouchers -> Flux
                         .fromIterable(eventVouchers)
                         .map(EventVoucher::getVoucherId)
                         .collectList()
                .flatMap(voucherIds -> voucherRepository
                         .findAllById(voucherIds)
                         .collectList()
                         .map(vouchers -> {
                             List<RetrieveVoucherResponse> voucherDTOs = vouchers
                                     .stream()
                                     .map(voucher -> RetrieveVoucherResponse.builder()
                                             .id(voucher.getId())
                                             .name(voucher.getName())
                                             .description(voucher.getDescription())
                                             .variableAmount(voucher.getVariableAmount())
                                             .startedAt(voucher.getStartedAt())
                                             .endedAt(voucher.getEndedAt())
                                             .build())
                                     .collect(Collectors.toList());

                         return RetrieveEventResponse.builder()
                                 .id(event.getId())
                                 .accountId(event.getAccountId())
                                 .accountName(account.getName())
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
                )))));
    }
}
