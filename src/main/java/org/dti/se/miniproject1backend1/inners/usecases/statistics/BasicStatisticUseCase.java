package org.dti.se.miniproject1backend1.inners.usecases.statistics;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.miniproject1backend1.outers.exceptions.statistics.StatisticAggregationInvalidException;
import org.dti.se.miniproject1backend1.outers.repositories.customs.StatisticCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BasicStatisticUseCase {

    @Autowired
    StatisticCustomRepository statisticCustomRepository;

    public Mono<List<StatisticSeriesResponse>> retrieveTransactionAmount(
            Account authenticatedAccount,
            String aggregation,
            String period
    ) {
        return Mono
                .fromCallable(() -> switch (aggregation) {
                    case "sum" -> statisticCustomRepository.retrieveTransactionAmountSum(authenticatedAccount, period);
                    case "average" ->
                            statisticCustomRepository.retrieveTransactionAmountAverage(authenticatedAccount, period);
                    default -> throw new StatisticAggregationInvalidException();
                })
                .flatMap(Flux::collectList)
                .as(Mono::from);
    }

    public Mono<List<StatisticSeriesResponse>> retrieveParticipantCount(
            Account authenticatedAccount,
            String aggregation,
            String period
    ) {
        return Mono
                .fromCallable(() -> switch (aggregation) {
                    case "sum" -> statisticCustomRepository.retrieveParticipantCountSum(authenticatedAccount, period);
                    case "average" ->
                            statisticCustomRepository.retrieveParticipantCountAverage(authenticatedAccount, period);
                    default -> throw new StatisticAggregationInvalidException();
                })
                .flatMap(Flux::collectList)
                .as(Mono::from);
    }
}
