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
                .uri("/participant/points?page=0&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveAllPointResponse>>>() {
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
                .uri("/participant/vouchers?page=0&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveAllVoucherResponse>>>() {
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
                .uri("/participant/feedbacks")
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
                .uri("/participant/feedbacks?page=0&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveAllFeedbackResponse>>>() {
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
    public  void testDeleteFeedback() {
        Feedback feedback = fakeFeedbacks.stream()
                .filter(f -> f.getAccountId().equals(authenticatedAccount.getId()))
                .findFirst().orElse(null);

        webTestClient
                .delete()
                .uri("/participant/feedbacks/{id}", feedback != null ? feedback.getId() : UUID.randomUUID())
                .exchange()
                .expectStatus()
                .isOk();
    }
}