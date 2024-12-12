package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.*;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.participant.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParticipantRestTest extends TestConfiguration {
    @BeforeAll
    public void beforeAll() {
        configure();
        populate();
        auth(fakeAccounts.getFirst());
    }

    @AfterAll
    public void afterAll() {
        deauth();
        depopulate();
    }

    @Test
    public void testRetrievePoints() {
        List<Point> points = fakePoints.stream()
                .filter(point -> point.getAccountId().equals(authenticatedAccount.getId()))
                .toList();

        webTestClient
                .get()
                .uri("/participants/points?page=0&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrievePointResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().size() == points.size();
                    body.getData().forEach(data -> {
                        assert points
                                .stream()
                                .anyMatch(point -> Objects.equals(point.getEndedAt(), data.getEndedAt()));
                    });
                });
    }

    @Test
    public void testRetrieveVouchers() {
        List<AccountVoucher> accountVouchers = fakeAccountVouchers.stream()
                .filter(accountVoucher -> accountVoucher.getAccountId().equals(authenticatedAccount.getId()))
                .toList();

        List<Voucher> vouchers = fakeVouchers.stream()
                .filter(voucher -> accountVouchers
                        .stream()
                        .anyMatch(accountVoucher -> accountVoucher.getVoucherId().equals(voucher.getId())))
                .toList();

        webTestClient
                .get()
                .uri("/participants/vouchers?page=0&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveVoucherResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().size() == accountVouchers.size();
                    body.getData().forEach(data -> {
                        assert vouchers
                                .stream()
                                .anyMatch(voucher -> Objects.equals(
                                        voucher.getCode(),
                                        data.getCode()));
                    });
                });
    }

    @Test
    public void testCreateFeedback() {
        Transaction transaction = fakeTransactions.stream()
                .filter(t -> t.getAccountId().equals(authenticatedAccount.getId())).skip(1)
                .findFirst().orElse(null);

        CreateFeedbackRequest createFeedbackRequest = CreateFeedbackRequest
                .builder()
                .transactionId(transaction != null ? transaction.getId() : UUID.randomUUID())
                .review("This is a test review")
                .rating(5)
                .build();

        webTestClient
                .post()
                .uri("/participants/feedbacks")
                .bodyValue(createFeedbackRequest)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<ResponseBody<CreateFeedbackResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().getId() != null;
                    assert body.getData().getTransactionId().equals(createFeedbackRequest.getTransactionId());
                    assert body.getData().getReview().equals(createFeedbackRequest.getReview());
                    assert Objects.equals(body.getData().getRating(), createFeedbackRequest.getRating());
                    fakeFeedbacks.add(Feedback.builder()
                            .id(body.getData().getId())
                            .transactionId(body.getData().getTransactionId())
                            .review(body.getData().getReview())
                            .rating(body.getData().getRating())
                            .build());
                });
    }

    @Test
    public void testRetrieveFeedbacks() {
        List<Transaction> transactions = fakeTransactions
                .stream().filter(transaction -> transaction.getAccountId().equals(authenticatedAccount.getId()))
                .toList();

        webTestClient
                .get()
                .uri("/participants/feedbacks?page=0&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveFeedbackResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().size() == transactions.size();
                    body.getData().forEach(data -> {
                        assert transactions
                                .stream()
                                .anyMatch(trx -> Objects.equals(trx.getId(), data.getTransactionId()));
                    });
                });
    }

    @Test
    public void testDeleteFeedback() {
        Feedback feedback = fakeFeedbacks.stream()
                .filter(f -> f.getAccountId().equals(authenticatedAccount.getId()))
                .findFirst().orElse(null);

        webTestClient
                .delete()
                .uri("/participants/feedbacks/{id}", feedback != null ? feedback.getId() : UUID.randomUUID())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testRetrieveTransactions() {
        List<Transaction> transactions = fakeTransactions
                .stream().filter(transaction -> transaction.getAccountId().equals(authenticatedAccount.getId()))
                .toList();

        webTestClient
                .get()
                .uri("/participants/transactions?page=0&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveTransactionResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().size() == transactions.size();
                    body.getData().forEach(data -> {
                        assert transactions
                                .stream()
                                .anyMatch(trx -> Objects.equals(trx.getId(), data.getTransactionId()));
                    });
                });
    }

    @Test
    public void testGetTransactionDetail() {
        Transaction transaction = fakeTransactions.stream()
                .filter(t -> t.getAccountId().equals(authenticatedAccount.getId())).skip(1)
                .findFirst().orElse(null);

        Event event = fakeEvents.stream()
                .filter(e -> {
                    assert e.getId() != null;
                    return e.getId().equals(Objects.requireNonNull(transaction).getEventId());
                })
                .findFirst().orElse(null);

        webTestClient
                .get()
                .uri("/participants/transactions/{id}", transaction != null ? transaction.getId() : UUID.randomUUID())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<TransactionDetailResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().getTransactionId().equals(Objects.requireNonNull(transaction).getId());
                    assert body.getData().getEventId().equals(Objects.requireNonNull(transaction).getEventId());
                    assert body.getData().getTime().equals(Objects.requireNonNull(event).getTime());
                });
    }

    @Test
    public void testGetTransactionEventDetail() {
        Transaction transaction = fakeTransactions.stream()
                .filter(t -> t.getAccountId().equals(authenticatedAccount.getId())).skip(1)
                .findFirst().orElse(null);

        Event event = fakeEvents.stream()
                .filter(e -> {
                    assert e.getId() != null;
                    return e.getId().equals(Objects.requireNonNull(transaction).getEventId());
                })
                .findFirst().orElse(null);

        webTestClient
                .get()
                .uri("/participants/transactions/{id}/events/{eventID}",
                        transaction != null ? transaction.getId() : UUID.randomUUID(),
                        Objects.requireNonNull(event).getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<TransactionEventDetailResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().getId().equals(Objects.requireNonNull(event).getId());
                    assert body.getData().getName().equals(Objects.requireNonNull(event).getName());
                    assert body.getData().getTime().equals(Objects.requireNonNull(event).getTime());
                });
    }
}
