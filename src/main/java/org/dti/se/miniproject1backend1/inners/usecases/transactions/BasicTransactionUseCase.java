package org.dti.se.miniproject1backend1.inners.usecases.transactions;

import org.dti.se.miniproject1backend1.inners.models.entities.*;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionCheckoutRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionCheckoutResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionTicketCheckoutResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionTicketFieldCheckoutResponse;
import org.dti.se.miniproject1backend1.outers.deliveries.holders.WebHolder;
import org.dti.se.miniproject1backend1.outers.exceptions.transactions.TicketSlotInsufficientException;
import org.dti.se.miniproject1backend1.outers.exceptions.transactions.VoucherQuantityInsufficientException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionExecution;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BasicTransactionUseCase {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventTicketRepository eventTicketRepository;

    @Autowired
    private EventTicketFieldRepository eventTicketFieldRepository;

    @Autowired
    private EventVoucherRepository eventVoucherRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    private AccountVoucherRepository accountVoucherRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionVoucherRepository transactionVoucherRepository;

    @Autowired
    private TransactionTicketFieldRepository transactionTicketFieldRepository;
    @Autowired
    private TransactionPointRepository transactionPointRepository;


    public Mono<TransactionCheckoutResponse> checkout(Account authenticatedAccount, TransactionCheckoutRequest request) {
        return eventRepository.
                findById(request.getEventId())
                .flatMap(event -> {
                    Mono<List<EventTicket>> eventTickets = eventTicketRepository.findAllByEventId(event.getId()).collectList();
                    return Mono.zip(Mono.just(event), eventTickets);
                })
                .flatMap(tuple -> {
                    List<UUID> eventTicketIds = tuple.getT2().stream().map(EventTicket::getId).toList();
                    Mono<List<EventTicketField>> eventTicketFields = eventTicketFieldRepository.findAllByEventTicketIdIn(eventTicketIds).collectList();
                    Mono<List<Point>> points = pointRepository.findAllByAccountId(authenticatedAccount.getId()).collectList();
                    Mono<List<Voucher>> vouchers = voucherRepository.findAllByCodeIn(request.getVoucherCodes()).collectList();
                    return Mono.zip(
                            Mono.zip(Mono.just(tuple.getT1()), Mono.just(tuple.getT2()), eventTicketFields),
                            Mono.zip(points, vouchers)
                    );
                })
                .flatMap(tuple -> {
                    List<UUID> voucherIds = request
                            .getVoucherCodes()
                            .stream()
                            .flatMap(code -> tuple
                                    .getT2()
                                    .getT2()
                                    .stream()
                                    .filter(voucher -> voucher.getCode().equals(code))
                                    .map(Voucher::getId)
                            )
                            .toList();
                    Mono<List<AccountVoucher>> accountVouchers = accountVoucherRepository.findAllById(voucherIds).collectList();
                    return Mono.zip(
                            Mono.just(tuple.getT1()),
                            Mono.zip(Mono.just(tuple.getT2().getT1()), Mono.just(tuple.getT2().getT2()), accountVouchers)
                    );
                })
                .flatMap(tuple -> {
                    for (EventTicket eventTicket : tuple.getT1().getT2()) {
                        if (eventTicket.getSlots() > 0) {
                            eventTicket.setSlots(eventTicket.getSlots() - 1);
                        } else {
                            return WebHolder
                                    .getTransaction()
                                    .doOnNext(TransactionExecution::setRollbackOnly)
                                    .then(Mono.error(new TicketSlotInsufficientException()));
                        }
                        eventTicket.setIsNew(false);
                    }
                    Mono<List<EventTicket>> updatedEventTickets = eventTicketRepository.saveAll(tuple.getT1().getT2()).collectList();
                    return Mono.zip(
                            Mono.zip(Mono.just(tuple.getT1().getT1()), updatedEventTickets, Mono.just(tuple.getT1().getT3())),
                            Mono.just(tuple.getT2())
                    );
                })
                .flatMap(tuple -> {
                    Double totalPrice = tuple
                            .getT1()
                            .getT2()
                            .stream()
                            .mapToDouble(EventTicket::getPrice)
                            .sum();
                    Double totalPoint = request.getPoints();
                    Double totalVoucher = request
                            .getVoucherCodes()
                            .stream()
                            .flatMap(code -> tuple
                                    .getT2()
                                    .getT2()
                                    .stream()
                                    .filter(voucher -> voucher.getCode().equals(code))
                            )
                            .map(voucher -> 1 - voucher.getVariableAmount())
                            .reduce(1.0, (a, b) -> a * b);
                    Double priceDeductedByPoint = Math.max(totalPrice - totalPoint, 0.0);
                    Double usedPoint = totalPoint - (totalPrice - priceDeductedByPoint);
                    Double priceDeductedByVoucher = Math.max(priceDeductedByPoint * totalVoucher, 0.0);

                    List<TransactionPoint> transactionPoints = new ArrayList<>();
                    Double usedPointRemaining = usedPoint;
                    for (Point point : tuple.getT2().getT1()) {
                        if (usedPointRemaining > 0) {
                            Double currentPoint = Math.min(usedPointRemaining, point.getFixedAmount());
                            usedPointRemaining -= currentPoint;
                            transactionPoints.add(TransactionPoint
                                    .builder()
                                    .id(UUID.randomUUID())
                                    .transactionId(null)
                                    .pointId(point.getId())
                                    .fixedAmount(currentPoint)
                                    .build()
                            );
                            point.setFixedAmount(point.getFixedAmount() - currentPoint);
                        }
                        point.setIsNew(false);
                    }

                    for (AccountVoucher accountVoucher : tuple.getT2().getT3()) {
                        if (accountVoucher.getQuantity() > 0) {
                            accountVoucher.setQuantity(accountVoucher.getQuantity() - 1);
                        } else {
                            return WebHolder
                                    .getTransaction()
                                    .doOnNext(TransactionExecution::setRollbackOnly)
                                    .then(Mono.error(new VoucherQuantityInsufficientException()));
                        }
                        accountVoucher.setIsNew(false);
                    }

                    Mono<List<Point>> updatedPoints = pointRepository.saveAll(tuple.getT2().getT1()).collectList();
                    Mono<List<AccountVoucher>> updatedAccountVouchers = accountVoucherRepository.saveAll(tuple.getT2().getT3()).collectList();
                    Mono<Double> finalPrice = Mono.just(priceDeductedByVoucher);

                    return Mono.zip(
                            Mono.just(tuple.getT1()),
                            Mono.zip(updatedPoints, Mono.just(transactionPoints), Mono.just(tuple.getT2().getT2()), updatedAccountVouchers, finalPrice)
                    );
                })
                .flatMap(tuple -> {
                    Transaction newTransaction = Transaction
                            .builder()
                            .id(UUID.randomUUID())
                            .accountId(authenticatedAccount.getId())
                            .eventId(tuple.getT1().getT1().getId())
                            .time(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS))
                            .build();

                    List<TransactionVoucher> transactionVouchers = tuple
                            .getT2()
                            .getT4()
                            .stream()
                            .map(accountVoucher -> TransactionVoucher
                                    .builder()
                                    .id(UUID.randomUUID())
                                    .transactionId(newTransaction.getId())
                                    .voucherId(accountVoucher.getVoucherId())
                                    .quantity((int) request
                                            .getVoucherCodes()
                                            .stream()
                                            .flatMap(code -> tuple
                                                    .getT2()
                                                    .getT3()
                                                    .stream()
                                                    .filter(voucher -> voucher.getCode().equals(code))
                                            )
                                            .count()
                                    )
                                    .build()
                            )
                            .toList();

                    List<TransactionTicketField> transactionTicketFields = request
                            .getTransactionTickets()
                            .stream()
                            .flatMap(transactionTicket -> transactionTicket
                                    .getFields()
                                    .stream()
                                    .map(field -> tuple
                                            .getT1()
                                            .getT3()
                                            .stream()
                                            .filter(eventTicketField -> eventTicketField.getEventTicketId().equals(transactionTicket.getEventTicketId()))
                                            .filter(eventTicketField -> eventTicketField.getKey().equals(field.getKey()))
                                            .findFirst()
                                            .map(eventTicketField -> TransactionTicketField
                                                    .builder()
                                                    .id(UUID.randomUUID())
                                                    .transactionId(newTransaction.getId())
                                                    .eventTicketFieldId(eventTicketField.getId())
                                                    .value(field.getValue())
                                                    .build()
                                            )
                                            .orElseThrow()
                                    )
                            )
                            .toList();

                    List<TransactionPoint> transactionPoints = tuple
                            .getT2()
                            .getT2()
                            .stream()
                            .map(transactionPoint -> transactionPoint.setTransactionId(newTransaction.getId()))
                            .toList();

                    Mono<Transaction> createdTransaction = transactionRepository.save(newTransaction);
                    Mono<List<TransactionPoint>> createdTransactionPoints = transactionPointRepository.saveAll(transactionPoints).collectList();
                    Mono<List<TransactionVoucher>> createdTransactionVouchers = transactionVoucherRepository.saveAll(transactionVouchers).collectList();
                    Mono<List<TransactionTicketField>> createdTransactionTicketFields = transactionTicketFieldRepository.saveAll(transactionTicketFields).collectList();
                    return Mono.zip(
                            Mono.just(tuple.getT1()),
                            Mono.just(tuple.getT2()),
                            Mono.zip(createdTransaction, createdTransactionVouchers, createdTransactionTicketFields, createdTransactionPoints)
                    );
                })
                .map(tuple -> {
                    List<TransactionTicketCheckoutResponse> transactionTickets = tuple
                            .getT1()
                            .getT2()
                            .stream()
                            .map(eventTicket -> TransactionTicketCheckoutResponse
                                    .builder()
                                    .id(eventTicket.getId())
                                    .eventTicketId(eventTicket.getId())
                                    .fields(request
                                            .getTransactionTickets()
                                            .stream()
                                            .filter(transactionTicket -> transactionTicket.getEventTicketId().equals(eventTicket.getId()))
                                            .flatMap(transactionTicket -> transactionTicket
                                                    .getFields()
                                                    .stream()
                                                    .map(field -> TransactionTicketFieldCheckoutResponse
                                                            .builder()
                                                            .key(field.getKey())
                                                            .value(field.getValue())
                                                            .build()
                                                    )
                                            )
                                            .toList()
                                    )
                                    .build()
                            )
                            .toList();
                    return TransactionCheckoutResponse
                            .builder()
                            .id(tuple.getT3().getT1().getId())
                            .eventId(tuple.getT1().getT1().getId())
                            .transactionTickets(transactionTickets)
                            .voucherCodes(request.getVoucherCodes())
                            .points(request.getPoints())
                            .finalPrice(tuple.getT2().getT5())
                            .build();
                });
    }

    public Mono<TransactionCheckoutResponse> tryCheckout(Account authenticatedAccount, TransactionCheckoutRequest request) {
        return checkout(authenticatedAccount, request)
                .flatMap(transaction ->
                        transactionRepository
                                .deleteById(transaction.getId())
                                .then(Mono.just(transaction))
                );
    }
}
