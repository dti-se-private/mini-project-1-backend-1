package org.dti.se.miniproject1backend1.inners.usecases.profile;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.dti.se.miniproject1backend1.inners.models.entities.Transaction;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.profile.CreateFeedbackRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.profile.RetrieveAllFeedbackResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.profile.RetrieveFeedbackResponse;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountUnAuthorizedException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.EventRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.FeedbackRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class BasicProfileUseCase {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    FeedbackRepository feedbackRepository;

    public Mono<List<RetrieveAllFeedbackResponse>> retrieveFeedbacks(
            Account claimerAccount,
            Integer page,
            Integer size) {
        return Mono.fromCallable(() -> transactionRepository
                        .findByAccountId(
                                claimerAccount.getId(),
                                PageRequest.of(page, size)
                        ))
                .flatMapMany(transactions -> transactions)
                .flatMap(transaction -> fetchFeedbackResponse(claimerAccount, transaction))
                .collectList();
    }

    public Mono<Void> createFeedback(Account claimerAccount, CreateFeedbackRequest request) {
        return feedbackRepository.findById(request.getId())
                .flatMap(feedback -> {
                    if (!feedback.getAccountId().equals(claimerAccount.getId())) {
                        return Mono.error(new AccountUnAuthorizedException());
                    }
                    feedback.setId(UUID.randomUUID());
                    feedback.setTransactionId(request.getTransactionId());
                    feedback.setAccountId(claimerAccount.getId());
                    feedback.setRating(request.getRating());
                    feedback.setReview(request.getReview());
                    return feedbackRepository.save(feedback);
                }).then();
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

    private Mono<RetrieveAllFeedbackResponse> fetchFeedbackResponse(Account claimerAccount, Transaction transaction) {
        return Mono.zip(eventRepository.findById(transaction.getEventId()), checkIfReviewed(claimerAccount, transaction))
                .map(tuple -> mapToFeedbackResponse(transaction, tuple.getT1(), tuple.getT2()));
    }

    private Mono<RetrieveFeedbackResponse> checkIfReviewed(Account claimerAccount, Transaction transaction) {
        return feedbackRepository.findByTransactionIdAndAccountId(transaction.getId(), claimerAccount.getId())
                .map(feedback -> RetrieveFeedbackResponse.builder()
                        .id(feedback.getId())
                        .rating(feedback.getRating())
                        .review(feedback.getReview())
                        .build());
    }

    private RetrieveAllFeedbackResponse mapToFeedbackResponse(
            Transaction transaction,
            Event event,
            RetrieveFeedbackResponse feedback) {
        return RetrieveAllFeedbackResponse.builder()
                .transactionId(transaction.getId())
                .eventId(event.getId())
                .eventName(event.getName())
                .time(event.getTime())
                .feedback(feedback)
                .build();
    }
}
