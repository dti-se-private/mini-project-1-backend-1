package org.dti.se.miniproject1backend1.inners.usecases.participant;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.dti.se.miniproject1backend1.inners.models.entities.Feedback;
import org.dti.se.miniproject1backend1.inners.models.entities.Transaction;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.participant.*;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountUnAuthorizedException;
import org.dti.se.miniproject1backend1.outers.exceptions.events.EventNotFoundException;
import org.dti.se.miniproject1backend1.outers.repositories.customs.EventCustomRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class BasicParticipantUseCase {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    FeedbackRepository feedbackRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    AccountVoucherRepository accountVoucherRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    TransactionPointRepository transactionPointRepository;

    @Autowired
    TransactionVoucherRepository transactionVoucherRepository;

    @Autowired
    EventCustomRepository eventCustomRepository;

    public Mono<List<RetrievePointResponse>> retrievePoints(
            Account claimerAccount,
            Integer page,
            Integer size
    ) {
        return pointRepository.findAllByAccountId(claimerAccount.getId(), PageRequest.of(page, size))
                .map(point -> RetrievePointResponse
                        .builder()
                        .fixedAmount(point.getFixedAmount())
                        .endedAt(point.getEndedAt())
                        .build()
                )
                .collectList();
    }

    public Mono<List<RetrieveVoucherResponse>> retrieveVouchers(
            Account claimerAccount,
            Integer page,
            Integer size
    ) {
        return accountVoucherRepository
                .findAllByAccountId(claimerAccount.getId(), PageRequest.of(page, size))
                .flatMap(accountVoucher -> voucherRepository
                        .findById(accountVoucher.getVoucherId())
                        .map(voucher -> RetrieveVoucherResponse
                                .builder()
                                .name(voucher.getName())
                                .description(voucher.getDescription())
                                .code(voucher.getCode())
                                .variableAmount(voucher.getVariableAmount())
                                .endedAt(voucher.getEndedAt())
                                .build()
                        )
                )
                .collectList();
    }

    public Mono<List<RetrieveFeedbackResponse>> retrieveFeedbacks(
            Account claimerAccount,
            Integer page,
            Integer size
    ) {
        return transactionRepository
                .findAllByAccountId(claimerAccount.getId(), PageRequest.of(page, size))
                .flatMap(transaction -> fetchFeedbackResponse(claimerAccount, transaction))
                .collectList();
    }

    public Mono<CreateFeedbackResponse> createFeedback(Account claimerAccount, CreateFeedbackRequest request) {
        return feedbackRepository.save(Feedback
                        .builder()
                        .id(UUID.randomUUID())
                        .transactionId(request.getTransactionId())
                        .accountId(claimerAccount.getId())
                        .rating(request.getRating())
                        .review(request.getReview())
                        .build()
                )
                .flatMap(feedback -> Mono
                        .just(CreateFeedbackResponse
                                .builder()
                                .id(feedback.getId())
                                .transactionId(feedback.getTransactionId())
                                .rating(feedback.getRating())
                                .review(feedback.getReview())
                                .build()
                        )
                );
    }

    public Mono<Void> deleteFeedback(Account claimerAccount, UUID feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .filter(feedback -> {
                    if (!feedback.getAccountId().equals(claimerAccount.getId())) {
                        throw new AccountUnAuthorizedException();
                    }
                    return true;
                })
                .flatMap(feedbackRepository::delete)
                .then();
    }

    public Mono<List<RetrieveTransactionResponse>> retrieveTransactions(
            Account claimerAccount,
            Integer page,
            Integer size
    ) {
        return transactionRepository
                .findAllByAccountId(claimerAccount.getId(), PageRequest.of(page, size))
                .flatMap(this::fetchTransactionResponse)
                .collectList();
    }

    public Mono<TransactionDetailResponse> getTransactionDetail(
            Account claimerAccount,
            UUID transactionId) {
        return transactionRepository
                .findById(transactionId)
                .filter(transaction -> transaction.getAccountId().equals(claimerAccount.getId()))
                .flatMap(this::fetchTransactionDetailResponse);
    }

    public Mono<TransactionEventDetailResponse> getTransactionEventDetail(
            Account claimerAccount,
            UUID transactionId,
            UUID eventId) {
        return transactionRepository
                .findById(transactionId)
                .filter(transaction -> transaction.getEventId().equals(eventId))
                .flatMap(transaction -> {
                    if (!transaction.getAccountId().equals(claimerAccount.getId())) {
                        return Mono.error(new AccountUnAuthorizedException());
                    }
                    return eventCustomRepository
                            .retrieveEventById(eventId)
                            .switchIfEmpty(Mono.error(new EventNotFoundException()));
                }).flatMap(event -> Mono.just(TransactionEventDetailResponse.builder()
                                .id(event.getId())
                                .name(event.getName())
                                .description(event.getDescription())
                                .time(event.getTime())
                                .location(event.getLocation())
                                .category(event.getCategory())
                                .eventTickets(event.getEventTickets())
                                .eventVouchers(event.getEventVouchers())
                                .build()
                        )
                );
    }

    private Mono<TransactionDetailResponse> fetchTransactionDetailResponse(Transaction transaction) {
        return Mono.zip(
                fetchUsedPointResponse(transaction),
                fetchUsedVoucherResponse(transaction),
                fetchTransactionResponse(transaction)
        ).map(tuple -> TransactionDetailResponse
                .builder()
                .transactionId(transaction.getId())
                .eventId(transaction.getEventId())
                .time(tuple.getT3().getTime())
                .usedPoints(tuple.getT1())
                .usedVouchers(tuple.getT2())
                .build()
        );
    }

    private Mono<List<UsedPointResponse>> fetchUsedPointResponse(Transaction transaction) {
        return transactionPointRepository
                .findByTransactionId(transaction.getId())
                .flatMap(transactionPoint -> pointRepository
                        .findById(transactionPoint.getPointId())
                        .map(point -> UsedPointResponse.builder()
                                .fixedAmount(transactionPoint.getFixedAmount())
                                .endedAt(point.getEndedAt())
                                .build()
                        )
                )
                .collectList();
    }

    private Mono<List<UsedVoucherResponse>> fetchUsedVoucherResponse(Transaction transaction) {
        return transactionVoucherRepository
                .findByTransactionId(transaction.getId())
                .flatMap(transactionVoucher -> voucherRepository
                        .findById(transactionVoucher.getVoucherId())
                        .map(voucher -> UsedVoucherResponse
                                .builder()
                                .name(voucher.getName())
                                .description(voucher.getDescription())
                                .code(voucher.getCode())
                                .variableAmount(voucher.getVariableAmount())
                                .endedAt(voucher.getEndedAt())
                                .build()
                        )
                )
                .collectList();
    }

    private Mono<RetrieveTransactionResponse> fetchTransactionResponse(Transaction transaction) {
        return eventRepository
                .findById(transaction.getEventId())
                .flatMap(event -> Mono
                        .just(RetrieveTransactionResponse
                                .builder()
                                .eventId(event.getId()).transactionId(transaction.getId())
                                .eventName(event.getName())
                                .time(event.getTime())
                                .build())
                );
    }

    private Mono<RetrieveFeedbackResponse> fetchFeedbackResponse(Account claimerAccount, Transaction transaction) {
        return Mono
                .zip(
                        eventRepository.findById(transaction.getEventId()),
                        checkIfReviewed(claimerAccount, transaction)
                                .defaultIfEmpty(Feedback
                                        .builder()
                                        .build()
                                )
                ).map(tuple -> mapToFeedbackResponse(transaction, tuple.getT1(), tuple.getT2()));
    }

    private Mono<Feedback> checkIfReviewed(Account claimerAccount, Transaction transaction) {
        return feedbackRepository.findByTransactionIdAndAccountId(transaction.getId(), claimerAccount.getId());
    }

    private RetrieveFeedbackResponse mapToFeedbackResponse(
            Transaction transaction,
            Event event,
            Feedback feedback) {
        return RetrieveFeedbackResponse
                .builder()
                .transactionId(transaction.getId())
                .eventId(event.getId())
                .eventName(event.getName())
                .time(event.getTime())
                .feedback(feedback)
                .build();
    }
}
