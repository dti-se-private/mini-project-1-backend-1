package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionCheckoutRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionCheckoutResponse;
import org.dti.se.miniproject1backend1.inners.usecases.transactions.BasicTransactionUseCase;
import org.dti.se.miniproject1backend1.outers.deliveries.holders.WebHolder;
import org.dti.se.miniproject1backend1.outers.exceptions.transactions.TicketSlotInsufficientException;
import org.dti.se.miniproject1backend1.outers.exceptions.transactions.VoucherQuantityInsufficientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/transactions")
public class TransactionRest {
    @Autowired
    BasicTransactionUseCase basicTransactionUseCase;

    @PostMapping("/checkout")
    public Mono<ResponseEntity<ResponseBody<TransactionCheckoutResponse>>> checkout(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestBody TransactionCheckoutRequest request
    ) {
        return basicTransactionUseCase
                .checkout(authenticatedAccount, request)
                .map(transaction -> ResponseBody
                        .<TransactionCheckoutResponse>builder()
                        .message("Checkout succeed.")
                        .data(transaction)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(TicketSlotInsufficientException.class, e -> Mono
                        .just(ResponseBody
                                .<TransactionCheckoutResponse>builder()
                                .message("Ticket slot is insufficient.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.BAD_REQUEST)
                        )
                )
                .onErrorResume(VoucherQuantityInsufficientException.class, e -> Mono
                        .just(ResponseBody
                                .<TransactionCheckoutResponse>builder()
                                .message("Voucher quantity is insufficient.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.BAD_REQUEST)
                        )
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<TransactionCheckoutResponse>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @PostMapping("/try-checkout")
    public Mono<ResponseEntity<ResponseBody<TransactionCheckoutResponse>>> tryCheckout(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestBody TransactionCheckoutRequest request
    ) {
        return basicTransactionUseCase
                .checkout(authenticatedAccount, request)
                .map(transaction -> ResponseBody
                        .<TransactionCheckoutResponse>builder()
                        .message("Checkout succeed.")
                        .data(transaction)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .flatMap((response) -> WebHolder
                        .getTransaction()
                        .doOnNext(ReactiveTransaction::setRollbackOnly)
                        .then(Mono.just(response))
                )
                .onErrorResume(TicketSlotInsufficientException.class, e -> Mono
                        .just(ResponseBody
                                .<TransactionCheckoutResponse>builder()
                                .message("Ticket slot is insufficient.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.BAD_REQUEST)
                        )
                )
                .onErrorResume(VoucherQuantityInsufficientException.class, e -> Mono
                        .just(ResponseBody
                                .<TransactionCheckoutResponse>builder()
                                .message("Voucher quantity is insufficient.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.BAD_REQUEST)
                        )
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<TransactionCheckoutResponse>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }
}
