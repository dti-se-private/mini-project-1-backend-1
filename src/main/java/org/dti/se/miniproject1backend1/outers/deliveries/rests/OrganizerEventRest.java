package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.usecases.events.OrganizerEventUseCase;
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
@RequestMapping(value = "/organizer/events")
public class OrganizerEventRest {
    @Autowired
    OrganizerEventUseCase organizerEventUseCase;

    @GetMapping
    public Mono<ResponseEntity<ResponseBody<List<RetrieveEventResponse>>>> retrieveMany(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam String page,
            @RequestParam String size
    ) {
        return organizerEventUseCase.retrieveEvents(page, size, authenticatedAccount.getId())
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
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> getEventDetail(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID id
    ) {
        return organizerEventUseCase.getEventById(id, authenticatedAccount.getId())
                .map(event -> ResponseBody.<RetrieveEventResponse>builder()
                        .message("Retrieve one event detail by id and organizer succeed.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono.just(ResponseBody.<RetrieveEventResponse>builder()
                        .message("Internal server error.")
                        .exception(e)
                        .build()
                        .toEntity(HttpStatus.INTERNAL_SERVER_ERROR))
                );
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> createNewEvent(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestBody CreateEventRequest request
    ) {
        return organizerEventUseCase.saveOne(request, authenticatedAccount.getId())
                .map(event -> ResponseBody.<RetrieveEventResponse>builder()
                        .message("Create new event by organizer succeed.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.CREATED)
                )
                .onErrorResume(e -> Mono.just(ResponseBody.<RetrieveEventResponse>builder()
                        .message("Internal server error.")
                        .exception(e)
                        .build()
                        .toEntity(HttpStatus.INTERNAL_SERVER_ERROR))
                );
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> updateEvent(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestBody RetrieveEventResponse request,
            @PathVariable UUID id
    ) {
        return Mono
                .fromCallable(() -> {
                    if (authenticatedAccount.getId() != null
                            && !authenticatedAccount.getId().equals(request.getOrganizerAccount().getId())) {
                        return null;
                    }
                    request.setId(id);
                    return request;
                })
                .switchIfEmpty(Mono.error(new AccountUnAuthorizedException("You are not the owner of the event.")))
                .flatMap(organizerEventUseCase::updateOne)
                .map(event -> ResponseBody.<RetrieveEventResponse>builder()
                        .message("Update event by organizer succeed.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> Mono.just(ResponseBody.<RetrieveEventResponse>builder()
                        .message("Internal server error.")
                        .exception(e)
                        .build()
                        .toEntity(HttpStatus.INTERNAL_SERVER_ERROR))
                );
    }
}