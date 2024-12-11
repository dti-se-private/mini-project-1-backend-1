package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.PatchEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.usecases.events.OrganizerEventUseCase;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountUnAuthorizedException;
import org.dti.se.miniproject1backend1.outers.exceptions.events.VoucherCodeExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/organizer/events")
public class OrganizerEventRest {
    @Autowired
    OrganizerEventUseCase organizerEventUseCase;

    @GetMapping
    public Mono<ResponseEntity<ResponseBody<List<RetrieveEventResponse>>>> retrieveEvents(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return organizerEventUseCase
                .retrieveEvents(authenticatedAccount, page, size)
                .map(eventList -> ResponseBody
                        .<List<RetrieveEventResponse>>builder()
                        .message("Retrieve many events by organizer succeed.")
                        .data(eventList)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<List<RetrieveEventResponse>>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> retrieveEvent(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID id
    ) {
        return organizerEventUseCase
                .retrieveEventById(authenticatedAccount, id)
                .map(event -> ResponseBody
                        .<RetrieveEventResponse>builder()
                        .message("Retrieve event by organizer succeed.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(AccountUnAuthorizedException.class, e -> Mono
                        .just(ResponseBody
                                .<RetrieveEventResponse>builder()
                                .message("Retrieve event by organizer is unauthorized.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.UNAUTHORIZED)
                        )
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<RetrieveEventResponse>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @PostMapping("")
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> createEvent(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestBody CreateEventRequest request
    ) {
        return organizerEventUseCase
                .createEvent(authenticatedAccount, request)
                .map(event -> ResponseBody
                        .<RetrieveEventResponse>builder()
                        .message("Create event by organizer succeed.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.CREATED)
                )
                .onErrorResume(VoucherCodeExistsException.class, e -> Mono
                        .just(ResponseBody
                                .<RetrieveEventResponse>builder()
                                .message("Voucher code exists.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.CONFLICT)
                        )
                )
                .onErrorResume(AccountNotFoundException.class, e -> Mono
                        .just(ResponseBody
                                .<RetrieveEventResponse>builder()
                                .message("Account not found.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.NOT_FOUND)
                        )
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<RetrieveEventResponse>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> patchEvent(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestBody PatchEventRequest request,
            @PathVariable UUID id
    ) {
        return organizerEventUseCase
                .patchEvent(authenticatedAccount, id, request)
                .map(event -> ResponseBody
                        .<RetrieveEventResponse>builder()
                        .message("Patch event by organizer succeed.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(VoucherCodeExistsException.class, e -> Mono
                        .just(ResponseBody
                                .<RetrieveEventResponse>builder()
                                .message("Voucher code exists.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.CONFLICT)
                        )
                )
                .onErrorResume(e -> Mono
                        .just(ResponseBody
                                .<RetrieveEventResponse>builder()
                                .message("Internal server error.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                        )
                );
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ResponseBody<Void>>> deleteEvent(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID id
    ) {
        return organizerEventUseCase
                .deleteEventById(authenticatedAccount, id)
                .thenReturn(ResponseBody
                        .<Void>builder()
                        .message("Delete event by organizer succeed.")
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(AccountUnAuthorizedException.class, e -> Mono
                        .just(ResponseBody
                                .<Void>builder()
                                .message("Delete event by organizer is unauthorized.")
                                .exception(e)
                                .build()
                                .toEntity(HttpStatus.UNAUTHORIZED)
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