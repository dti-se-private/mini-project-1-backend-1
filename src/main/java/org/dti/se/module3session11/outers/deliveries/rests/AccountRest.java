package org.dti.se.module3session11.outers.deliveries.rests;

import org.dti.se.module3session11.inners.models.entities.Account;
import org.dti.se.module3session11.inners.models.valueobjects.ResponseBody;
import org.dti.se.module3session11.inners.usecases.AccountUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping(value = "/accounts")
public class AccountRest {
    @Autowired
    private AccountUseCase accountUseCase;

    @PostMapping
    public Mono<ResponseEntity<ResponseBody<Account>>> saveOne(
            @RequestBody Account account
    ) {
        return accountUseCase
                .saveOne(account)
                .map(savedAccount -> ResponseBody
                        .<Account>builder()
                        .message("Account saved.")
                        .data(savedAccount)
                        .build()
                        .toEntity(HttpStatus.CREATED)
                )
                .onErrorResume(e -> Mono
                        .fromCallable(() -> switch (e.getClass().getSimpleName()) {
                            case "AccountExistException" -> ResponseBody
                                    .<Account>builder()
                                    .message("Account already exists.")
                                    .build()
                                    .toEntity(HttpStatus.CONFLICT);
                            default -> ResponseBody
                                    .<Account>builder()
                                    .message("Internal server error.")
                                    .error(e)
                                    .build()
                                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                        })
                );
    }

    @GetMapping(value = "/{id}")
    public Mono<ResponseEntity<ResponseBody<Account>>> findOneById(
            @PathVariable("id") UUID id
    ) {
        return accountUseCase
                .findOneById(id)
                .map(foundAccount -> ResponseBody
                        .<Account>builder()
                        .message("Account found.")
                        .data(foundAccount)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .fromCallable(() -> switch (e.getClass().getSimpleName()) {
                            case "AccountNotFoundException" -> ResponseBody
                                    .<Account>builder()
                                    .message("Account not found.")
                                    .build()
                                    .toEntity(HttpStatus.NOT_FOUND);
                            default -> ResponseBody
                                    .<Account>builder()
                                    .message("Internal server error.")
                                    .error(e)
                                    .build()
                                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                        })
                );
    }

    @PatchMapping(value = "/{id}")
    public Mono<ResponseEntity<ResponseBody<Account>>> patchOneById(
            @PathVariable("id") UUID id,
            @RequestBody Account account
    ) {
        return accountUseCase
                .patchOneById(id, account)
                .map(updatedAccount -> ResponseBody
                        .<Account>builder()
                        .message("Account patched.")
                        .data(updatedAccount)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .fromCallable(() -> switch (e.getClass().getSimpleName()) {
                            case "AccountNotFoundException" -> ResponseBody
                                    .<Account>builder()
                                    .message("Account not found")
                                    .build()
                                    .toEntity(HttpStatus.NOT_FOUND);
                            default -> ResponseBody
                                    .<Account>builder()
                                    .message("Internal server error.")
                                    .error(e)
                                    .build()
                                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                        })
                );
    }

    @DeleteMapping(value = "/{id}")
    public Mono<ResponseEntity<ResponseBody<Void>>> deleteOneById(
            @PathVariable("id") UUID id
    ) {
        return accountUseCase
                .deleteOneById(id)
                .then(Mono
                        .fromCallable(() -> ResponseBody
                                .<Void>builder()
                                .message("Account deleted.")
                                .build()
                                .toEntity(HttpStatus.OK))
                )
                .onErrorResume(e -> Mono
                        .fromCallable(() -> switch (e.getClass().getSimpleName()) {
                            default -> ResponseBody
                                    .<Void>builder()
                                    .message("Internal server error.")
                                    .error(e)
                                    .build()
                                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                        })
                );
    }

}
