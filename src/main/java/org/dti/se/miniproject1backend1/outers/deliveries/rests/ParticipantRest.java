package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.participant.*;
import org.dti.se.miniproject1backend1.inners.usecases.participant.BasicParticipantUseCase;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountUnAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/participant")
public class ParticipantRest {
    @Autowired
    BasicParticipantUseCase basicProfileUseCase;

    @GetMapping("/points")
    public Mono<ResponseEntity<ResponseBody<List<RetrieveAllPointResponse>>>> retrievePoints(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return basicProfileUseCase.retrievePoints(authenticatedAccount, page, size)
                .map(points -> ResponseBody
                        .<List<RetrieveAllPointResponse>>builder()
                        .message("Points retrieved.")
                        .data(points)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<List<RetrieveAllPointResponse>>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @GetMapping("/vouchers")
    public Mono<ResponseEntity<ResponseBody<List<RetrieveAllVoucherResponse>>>> retrieveVouchers(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return basicProfileUseCase.retrieveVouchers(authenticatedAccount, page, size)
                .map(vouchers -> ResponseBody
                        .<List<RetrieveAllVoucherResponse>>builder()
                        .message("Vouchers retrieved.")
                        .data(vouchers)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<List<RetrieveAllVoucherResponse>>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @GetMapping("/feedbacks")
    public Mono<ResponseEntity<ResponseBody<List<RetrieveAllFeedbackResponse>>>> retrieveFeedbacks(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return basicProfileUseCase.retrieveFeedbacks(authenticatedAccount, page, size)
                .map(feedbacks -> ResponseBody
                        .<List<RetrieveAllFeedbackResponse>>builder()
                        .message("Feedbacks retrieved.")
                        .data(feedbacks)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<List<RetrieveAllFeedbackResponse>>builder()
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
        return basicProfileUseCase.createFeedback(authenticatedAccount, request)
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
        return basicProfileUseCase.deleteFeedback(authenticatedAccount, id)
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
}
