package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.entities.EventVoucher;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventVoucherResponse;
import org.dti.se.miniproject1backend1.outers.repositories.customs.EventCustomRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    EventCustomRepository eventCustomRepository;

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
                                        .name(event.getName())
                                        .location(event.getLocation())
                                        .category(event.getCategory())
                                        .time(event.getTime())
                                        .build()
                                )
                        ));
    }

    public Mono<List<RetrieveEventResponse>> retrieveEvents(Integer page, Integer size, List<String> filters, String search) {
        return Mono
                .fromCallable(() -> {
                    if (filters.isEmpty()) {
                        filters.addAll(List.of("name", "description", "category", "time", "location"));
                    }
                    return filters;
                })
                .flatMap(queryFilters -> eventCustomRepository
                        .retrieveEvents(page, size, queryFilters, search)
                        .collectList()
                );
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
                                                            List<RetrieveEventVoucherResponse> voucherDTOs = vouchers
                                                                    .stream()
                                                                    .map(voucher -> RetrieveEventVoucherResponse.builder()
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
                                                                    .name(event.getName())
                                                                    .description(event.getDescription())
                                                                    .location(event.getLocation())
                                                                    .category(event.getCategory())
                                                                    .time(event.getTime())
                                                                    .eventVouchers(voucherDTOs)
                                                                    .build();
                                                        })
                                                )))));
    }
}
