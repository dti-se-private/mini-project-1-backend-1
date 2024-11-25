package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.usecases.events.BasicEventUseCase;
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
    BasicEventUseCase basicEventUseCase;

    @GetMapping("/hero")
    public Mono<ResponseEntity<ResponseBody<List<RetrieveEventResponse>>>> getHero() {
        return basicEventUseCase.getTop3Events()
                .collectList()
                .map(eventList -> ResponseBody.<List<RetrieveEventResponse>>builder()
                        .message("Hero fetched.")
                        .data(eventList)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> {
                    ResponseBody<List<RetrieveEventResponse>> responseBody = ResponseBody
                            .<List<RetrieveEventResponse>>builder()
                            .message("Error occurred while fetching hero events.")
                            .exception(e)
                            .data(Collections.emptyList())
                            .build();
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(responseBody));
                });
    }

    @GetMapping
    public Mono<ResponseEntity<ResponseBody<List<RetrieveEventResponse>>>> getAllByCategory(@RequestParam(required = false) String category) {
        return basicEventUseCase.getAllEvents(category)
                .collectList()
                .map(eventList -> ResponseBody.<List<RetrieveEventResponse>>builder()
                        .message("Events by category fetched.")
                        .data(eventList)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> {
                    ResponseBody<List<RetrieveEventResponse>> responseBody = ResponseBody
                            .<List<RetrieveEventResponse>>builder()
                            .message("Error occurred while fetching events by category.")
                            .exception(e)
                            .data(Collections.emptyList())
                            .build();
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(responseBody));
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> getEventDetail(@PathVariable UUID id) {
        return basicEventUseCase.getEventById(id)
                .map(event -> ResponseBody.<RetrieveEventResponse>builder()
                        .message("Events by category fetched.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.OK)
                )
                .onErrorResume(e -> {
                    ResponseBody<RetrieveEventResponse> responseBody = ResponseBody
                            .<RetrieveEventResponse>builder()
                            .message("Error occurred while fetching events by category.")
                            .exception(e)
                            .data(null)
                            .build();
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(responseBody));
                });
    }
}
