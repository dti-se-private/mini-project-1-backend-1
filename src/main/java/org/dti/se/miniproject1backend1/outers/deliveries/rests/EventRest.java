package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.EventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.usecases.EventUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/events")
public class EventRest {
    @Autowired
    EventUseCase eventUseCase;

    @GetMapping("/hero")
    public Mono<ResponseEntity<ResponseBody<List<EventResponse>>>> getHero() {
        return eventUseCase.getTop3Events()
                .collectList()
                .map(eventList -> ResponseBody.<List<EventResponse>>builder()
                        .message("Hero fetched.")
                        .data(eventList)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> {
                    ResponseBody<List<EventResponse>> responseBody = ResponseBody
                            .<List<EventResponse>>builder()
                            .message("Error occurred while fetching hero events.")
                            .error(e)
                            .data(Collections.emptyList())
                            .build();
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(responseBody));
                });
    }

    @GetMapping
    public Mono<ResponseEntity<ResponseBody<List<EventResponse>>>> getAllByCategory(@RequestParam(required = false) String category) {
        return eventUseCase.getAllEvents(category)
                .collectList()
                .map(eventList -> ResponseBody.<List<EventResponse>>builder()
                        .message("Events by category fetched.")
                        .data(eventList)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> {
                    ResponseBody<List<EventResponse>> responseBody = ResponseBody
                            .<List<EventResponse>>builder()
                            .message("Error occurred while fetching events by category.")
                            .error(e)
                            .data(Collections.emptyList())
                            .build();
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(responseBody));
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ResponseBody<EventResponse>>> getEventDetail(@PathVariable UUID id) {
        return eventUseCase.getEventById(id)
                .map(event -> ResponseBody.<EventResponse>builder()
                        .message("Events by category fetched.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> {
                    ResponseBody<EventResponse> responseBody = ResponseBody
                            .<EventResponse>builder()
                            .message("Error occurred while fetching events by category.")
                            .error(e)
                            .data(null)
                            .build();
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(responseBody));
                });
    }
}
