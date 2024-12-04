package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.usecases.events.BasicEventUseCase;
import org.dti.se.miniproject1backend1.inners.usecases.events.OrganizerEventUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
            String page,
            String size
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
                .onErrorResume(e -> {
                    String message = e.getMessage().equals("unauthorized")
                            ? "You are not the owner of the event."
                            : "Internal server error.";
                    HttpStatus status = e.getMessage().equals("unauthorized")
                            ? HttpStatus.UNAUTHORIZED
                            : HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(ResponseBody.<RetrieveEventResponse>builder()
                            .message(message)
                            .exception(e)
                            .build()
                            .toEntity(status));
                });
    }

    @PostMapping
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> createNewEvent(
            @AuthenticationPrincipal Account authenticatedAccount,
            @Validated CreateEventRequest request
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
}