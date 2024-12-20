package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.participant.*;
import org.dti.se.miniproject1backend1.inners.usecases.participant.BasicParticipantUseCase;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountUnAuthorizedException;
import org.dti.se.miniproject1backend1.outers.exceptions.events.EventNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/participants")
public class ParticipantRest {
    @Autowired
    BasicParticipantUseCase basicProfileUseCase;

    @GetMapping("/points")
    public Mono<ResponseEntity<ResponseBody<List<RetrievePointResponse>>>> retrievePoints(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return basicProfileUseCase
                .retrievePoints(authenticatedAccount, page, size)
                .map(points -> ResponseBody
                        .<List<RetrievePointResponse>>builder()
                        .message("Points retrieved.")
                        .data(points)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<List<RetrievePointResponse>>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @GetMapping("/vouchers")
    public Mono<ResponseEntity<ResponseBody<List<RetrieveVoucherResponse>>>> retrieveVouchers(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return basicProfileUseCase
                .retrieveVouchers(authenticatedAccount, page, size)
                .map(vouchers -> ResponseBody
                        .<List<RetrieveVoucherResponse>>builder()
                        .message("Vouchers retrieved.")
                        .data(vouchers)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<List<RetrieveVoucherResponse>>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @GetMapping("/feedbacks")
    public Mono<ResponseEntity<ResponseBody<List<RetrieveFeedbackResponse>>>> retrieveFeedbacks(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return basicProfileUseCase
                .retrieveFeedbacks(authenticatedAccount, page, size)
                .map(feedbacks -> ResponseBody
                        .<List<RetrieveFeedbackResponse>>builder()
                        .message("Feedbacks retrieved.")
                        .data(feedbacks)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<List<RetrieveFeedbackResponse>>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }


    @PostMapping("/feedbacks")
    public Mono<ResponseEntity<ResponseBody<CreateFeedbackResponse>>> createFeedback(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestBody CreateFeedbackRequest request
    ) {
        return basicProfileUseCase
                .createFeedback(authenticatedAccount, request)
                .map(feedback -> ResponseBody
                        .<CreateFeedbackResponse>builder()
                        .message("Feedback created.")
                        .data(feedback)
                        .build()
                        .toEntity(HttpStatus.CREATED))
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<CreateFeedbackResponse>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @DeleteMapping("/feedbacks/{id}")
    public Mono<ResponseEntity<ResponseBody<Void>>> deleteFeedback(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID id
    ) {
        return basicProfileUseCase
                .deleteFeedback(authenticatedAccount, id)
                .then(Mono.fromCallable(() -> ResponseBody
                        .<Void>builder()
                        .message("Feedback deleted.")
                        .data(null)
                        .build()
                        .toEntity(HttpStatus.OK)))
                .onErrorResume(AccountUnAuthorizedException.class, e -> Mono
                        .just(ResponseBody
                                .<Void>builder()
                                .message("Feedback does not belong to the given account.")
                                .build()
                                .toEntity(HttpStatus.CONFLICT)
                        )
                )
                .onErrorResume(EventNotFoundException.class, e -> Mono
                        .just(ResponseBody
                                .<Void>builder()
                                .message("Event not found.")
                                .build()
                                .toEntity(HttpStatus.CONFLICT)
                        )
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<Void>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @GetMapping("/transactions")
    public Mono<ResponseEntity<ResponseBody<List<RetrieveTransactionResponse>>>> retrieveTransactions(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return basicProfileUseCase
                .retrieveTransactions(authenticatedAccount, page, size)
                .map(transactions -> ResponseBody
                        .<List<RetrieveTransactionResponse>>builder()
                        .message("Transactions retrieved.")
                        .data(transactions)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<List<RetrieveTransactionResponse>>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @GetMapping("/transactions/{id}")
    public Mono<ResponseEntity<ResponseBody<TransactionDetailResponse>>> getTransactionDetail(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID id
    ) {
        return basicProfileUseCase
                .getTransactionDetail(authenticatedAccount, id)
                .map(transaction -> ResponseBody
                        .<TransactionDetailResponse>builder()
                        .message("Transaction Detail retrieved.")
                        .data(transaction)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<TransactionDetailResponse>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @GetMapping("/transactions/{transactionId}/events/{eventId}")
    public Mono<ResponseEntity<ResponseBody<TransactionEventDetailResponse>>> getTransactionDetail(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID transactionId,
            @PathVariable UUID eventId
    ) {
        return basicProfileUseCase
                .getTransactionEventDetail(authenticatedAccount, transactionId, eventId)
                .map(transaction -> ResponseBody
                        .<TransactionEventDetailResponse>builder()
                        .message("Transaction Detail retrieved.")
                        .data(transaction)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(AccountUnAuthorizedException.class, e -> Mono
                        .just(ResponseBody
                                .<TransactionEventDetailResponse>builder()
                                .message("Feedback does not belong to the given account.")
                                .build()
                                .toEntity(HttpStatus.CONFLICT)
                        )
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<TransactionEventDetailResponse>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }
}
