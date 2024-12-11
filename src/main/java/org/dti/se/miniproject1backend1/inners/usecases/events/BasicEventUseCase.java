package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.outers.exceptions.events.EventNotFoundException;
import org.dti.se.miniproject1backend1.outers.repositories.customs.EventCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class BasicEventUseCase {
    @Autowired
    EventCustomRepository eventCustomRepository;

    public Mono<List<RetrieveEventResponse>> retrieveEvents(Integer page, Integer size, List<String> filters, String search) {
        return Mono
                .fromCallable(() -> {
                    if (filters.isEmpty()) {
                        filters.addAll(List.of("name", "description", "category", "time", "location"));
                    }
                    return filters;
                })
                .flatMap(queryFilters -> eventCustomRepository
                        .retrieveEvents(page, size, queryFilters, search)
                        .map(event -> event.setEventParticipants(null))
                        .collectList()
                );
    }

    public Mono<RetrieveEventResponse> retrieveEventById(UUID id) {
        return eventCustomRepository
                .retrieveEventById(id)
                .map(event -> event.setEventParticipants(null))
                .switchIfEmpty(Mono.error(new EventNotFoundException()));
    }
}
