package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
public class OrganizerEventUseCase {
    @Autowired
    EventRepository eventRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    EventVoucherRepository eventVoucherRepository;

    @Autowired
    EventTicketRepository eventTicketRepository;

    @Autowired
    EventTicketFieldRepository eventTicketFieldRepository;

    @Autowired
    @Qualifier("oneTransactionalOperator")
    private TransactionalOperator transactionalOperator;

    public Mono<CreateEventRequest> saveOne(CreateEventRequest request) {
        return null;/*transactionalOperator.execute(transaction ->
                eventRepository.save(
                        Event.builder()
                                .accountId(request.getAccountId())
                                .name(request.getName())
                                .description(request.getDescription())
                                .location(request.getLocation())
                                .category(request.getCategory())
                                .time(request.getTime())
                                .build()
                ).flatMap(savedEvent -> {
                    if (request.getVouchers() != null) {
                        return Mono.when(
                                Arrays.stream(request.getVouchers())
                                        .map(voucherRequest ->
                                                voucherRepository.save(
                                                        Voucher.builder()
                                                                .code("lll")
                                                                .name(voucherRequest.getName())
                                                                .description(voucherRequest.getDescription())
                                                                .variableAmount(voucherRequest.getVariableAmount())
                                                                .startedAt(voucherRequest.getStartedAt())
                                                                .endedAt(voucherRequest.getEndedAt())
                                                                .build()
                                                )
                                        )
                                        .toArray(Mono[]::new) // Convert the stream to an array of Monos
                        ).thenReturn(request); // Return the original request after saving vouchers
                    }
                    return Mono.just(request); // Return the original request if no vouchers
                })
        );*/
    }
}
