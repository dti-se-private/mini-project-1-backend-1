package org.dti.se.miniproject1backend1.inners.usecases.events;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.usecases.authentications.JwtAuthenticationUseCase;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.UnauthorizedAccessException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizerEventUseCase {
    @Autowired
    JwtAuthenticationUseCase jwtAuthenticationUseCase;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    BasicEventUseCase basicEventUseCase;

    public Mono<List<RetrieveEventResponse>> retrieveEvents(Session session, String page, String size) {
        return Mono
                .fromCallable(() -> jwtAuthenticationUseCase.verify(session.getAccessToken()))
                .map(decodedJwt -> decodedJwt.getClaim("account_id").as(UUID.class))
                .flatMap(accountId -> accountRepository.findFirstById(accountId))
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(account -> {
                    int pageNumber = (page != null && !page.isEmpty()) ? Integer.parseInt(page) : 0;
                    int pageSize = (size != null && !size.isEmpty()) ? Integer.parseInt(size) : 10;
                    Pageable pageable = PageRequest.of(pageNumber, pageSize);

                    return eventRepository.findByAccountId(account.getId(), pageable)
                            .map(event -> RetrieveEventResponse.builder()
                                    .id(event.getId())
                                    .name(event.getName())
                                    .time(event.getTime())
                                    .build())
                            .collectList();
                });
    }

    public Mono<RetrieveEventResponse> getEventById(UUID eventID, Session session) {
        return Mono
                .fromCallable(() -> jwtAuthenticationUseCase.verify(session.getAccessToken()))
                .map(decodedJwt -> decodedJwt.getClaim("account_id").as(UUID.class))
                .flatMap(accountId -> accountRepository.findFirstById(accountId))
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(account -> basicEventUseCase.getEventById(eventID)
                        .flatMap(event -> {
                            if (event.getOrganizerAccount().getId().equals(account.getId())) {
                                return Mono.just(event);
                            } else {
                                return Mono.error(new UnauthorizedAccessException("You are not the owner of the event"));
                            }
                        }));
    }

    public Mono<CreateEventRequest> saveOne(CreateEventRequest request) {
        //check if the organizer is the owner of the event
        //check if the event is not exist
        //save the event
        //return event detail from getEventById
        return null;
    }

    public Mono<CreateEventRequest> updateOne(RetrieveEventResponse request) {
        //check if the organizer is the owner of the event
        //check if the event exist
        //update the event
        //return event detail from getEventById
        return null;
    }
}
