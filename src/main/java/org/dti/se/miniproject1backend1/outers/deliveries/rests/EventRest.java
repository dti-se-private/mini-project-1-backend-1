package org.dti.se.miniproject1backend1.outers.deliveries.rests;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.usecases.events.BasicEventUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
                        .message("Retrieve top 3 events succeed.")
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

    @GetMapping
    public Mono<ResponseEntity<ResponseBody<List<RetrieveEventResponse>>>> retrieveMany(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") List<String> filters,
            @RequestParam(defaultValue = "") String search
    ) {
        return basicEventUseCase.retrieveEvents(page, size, filters, search)
                .map(eventList -> ResponseBody
                        .<List<RetrieveEventResponse>>builder()
                        .message("Retrieve many events succeed.")
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
    public Mono<ResponseEntity<ResponseBody<RetrieveEventResponse>>> getEventDetail(@PathVariable UUID id) {
        return basicEventUseCase.getEventById(id)
                .map(event -> ResponseBody.<RetrieveEventResponse>builder()
                        .message("Retrieve one event by id succeed.")
                        .data(event)
                        .build()
                        .toEntity(HttpStatus.OK)
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
}
