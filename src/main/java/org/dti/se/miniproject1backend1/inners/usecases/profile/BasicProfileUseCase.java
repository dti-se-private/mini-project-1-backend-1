package org.dti.se.miniproject1backend1.inners.usecases.profile;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.profile.RetrieveFeedbackResponse;
import org.dti.se.miniproject1backend1.outers.repositories.ones.EventRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BasicProfileUseCase {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    EventRepository eventRepository;

    public Mono<List<RetrieveFeedbackResponse>> retrieveFeedbacks(Account claimerAccount) {
        return null;
    }
}
