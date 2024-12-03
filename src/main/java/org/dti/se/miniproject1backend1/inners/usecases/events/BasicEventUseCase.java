package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.entities.EventVoucher;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventTicketResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventVoucherResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveOrganizerAccountResponse;
import org.dti.se.miniproject1backend1.outers.repositories.customs.EventCustomRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        return eventRepository.findById(eventID)
                .flatMap(event -> {
                    Mono<Account> accountMono = accountRepository.findById(event.getAccountId());

                    Mono<List<RetrieveEventVoucherResponse>> vouchersMono = eventVoucherRepository.findByEventId(event.getId())
                            .map(EventVoucher::getVoucherId)
                            .collectList()
                            .flatMapMany(voucherIds -> voucherRepository.findAllById(voucherIds)) // Use flatMapMany to handle the Flux<Voucher>
                            .collectList()
                            .map(vouchers -> vouchers.stream()
                                    .map(voucher -> RetrieveEventVoucherResponse.builder()
                                            .id(voucher.getId())
                                            .name(voucher.getName())
                                            .description(voucher.getDescription())
                                            .variableAmount(voucher.getVariableAmount())
                                            .startedAt(voucher.getStartedAt())
                                            .endedAt(voucher.getEndedAt())
                                            .build())
                                    .collect(Collectors.toList()));

                    Mono<List<RetrieveEventTicketResponse>> ticketsMono = eventTicketRepository.findByEventId(event.getId())
                            .flatMapMany(ticket -> Mono.just(RetrieveEventTicketResponse.builder()
                                    .slots(ticket.getSlots())
                                    .price(ticket.getPrice())
                                    .build()))
                            .collectList();

                    Mono<Integer> participantCountMono = transactionRepository.countByEventId(event.getId())
                            .defaultIfEmpty(0);

                    return Mono.zip(accountMono, vouchersMono, ticketsMono, participantCountMono)
                            .map(tuple -> {
                                Account account = tuple.getT1();
                                List<RetrieveEventVoucherResponse> vouchers = tuple.getT2();
                                List<RetrieveEventTicketResponse> tickets = tuple.getT3();
                                Integer participantCount = tuple.getT4();

                                return RetrieveEventResponse.builder()
                                        .id(event.getId())
                                        .name(event.getName())
                                        .description(event.getDescription())
                                        .location(event.getLocation())
                                        .category(event.getCategory())
                                        .time(event.getTime())
                                        .organizerAccount(RetrieveOrganizerAccountResponse.builder()
                                                .name(account.getName())
                                                .build())
                                        .eventTickets(tickets)
                                        .numberOfParticipants(participantCount)
                                        .eventVouchers(vouchers)
                                        .build();
                            });
                });
    }
}
