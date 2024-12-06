package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.dti.se.miniproject1backend1.inners.models.entities.Point;
import org.dti.se.miniproject1backend1.inners.models.entities.Voucher;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionCheckoutRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionCheckoutResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionTicketCheckoutRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions.TransactionTicketFieldCheckoutRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.UUID;

public class TransactionRestTest extends TestConfiguration {
    @BeforeEach
    public void beforeEach() {
        configure();
        populate();
        auth();
    }

    @AfterEach
    public void afterEach() {
        deauth();
        depopulate();
    }

    @Test
    public void testCheckout() {
        Event event = fakeEvents.getFirst();

        List<TransactionTicketCheckoutRequest> transactionTickets = fakeEventTickets
                .stream()
                .filter(eventTicket -> eventTicket.getEventId().equals(event.getId()))
                .map(eventTicket -> {
                    List<TransactionTicketFieldCheckoutRequest> fields = fakeEventTicketFields
                            .stream()
                            .filter(eventTicketField -> eventTicketField
                                    .getEventTicketId()
                                    .equals(eventTicket.getId())
                            )
                            .map(eventTicketField -> TransactionTicketFieldCheckoutRequest
                                    .builder()
                                    .key(eventTicketField.getKey())
                                    .value("%s-%s".formatted(eventTicketField.getKey(), UUID.randomUUID().toString()))
                                    .build()
                            )
                            .toList();

                    return TransactionTicketCheckoutRequest
                            .builder()
                            .eventTicketId(eventTicket.getId())
                            .fields(fields)
                            .build();
                })
                .toList();

        List<String> voucherCodes = fakeAccountVouchers
                .stream()
                .filter(accountVoucher -> accountVoucher.getAccountId().equals(authenticatedAccount.getId()))
                .flatMap(accountVoucher -> fakeVouchers
                        .stream()
                        .filter(voucher -> voucher.getId().equals(accountVoucher.getVoucherId()))
                        .map(Voucher::getCode)
                )
                .toList();

        Double points = fakePoints
                .stream()
                .filter(point -> point.getAccountId().equals(authenticatedAccount.getId()))
                .map(Point::getFixedAmount)
                .reduce(0.0, Double::sum);

        TransactionCheckoutRequest request = TransactionCheckoutRequest
                .builder()
                .eventId(event.getId())
                .transactionTickets(transactionTickets)
                .voucherCodes(voucherCodes)
                .points(points)
                .build();

        webTestClient
                .post()
                .uri("/transactions/checkout")
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<TransactionCheckoutResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals("Checkout succeed.");
                    assert body.getData() != null;
                    assert body.getData().getId() != null;
                    assert body.getData().getEventId().equals(event.getId());
                    assert body.getData().getTransactionTickets().size() == transactionTickets.size();
                    assert voucherCodes.containsAll(body.getData().getVoucherCodes());
                    assert body.getData().getPoints() <= points;
                    assert body.getData().getFinalPrice() >= 0.0;
                });
    }


    @Test
    public void testTryCheckout() {
        Event event = fakeEvents.getFirst();

        List<TransactionTicketCheckoutRequest> transactionTickets = fakeEventTickets
                .stream()
                .filter(eventTicket -> eventTicket.getEventId().equals(event.getId()))
                .map(eventTicket -> {
                    List<TransactionTicketFieldCheckoutRequest> fields = fakeEventTicketFields
                            .stream()
                            .filter(eventTicketField -> eventTicketField
                                    .getEventTicketId()
                                    .equals(eventTicket.getId())
                            )
                            .map(eventTicketField -> TransactionTicketFieldCheckoutRequest
                                    .builder()
                                    .key(eventTicketField.getKey())
                                    .value("%s-%s".formatted(eventTicketField.getKey(), UUID.randomUUID().toString()))
                                    .build()
                            )
                            .toList();

                    return TransactionTicketCheckoutRequest
                            .builder()
                            .eventTicketId(eventTicket.getId())
                            .fields(fields)
                            .build();
                })
                .toList();

        List<String> voucherCodes = fakeAccountVouchers
                .stream()
                .filter(accountVoucher -> accountVoucher.getAccountId().equals(authenticatedAccount.getId()))
                .flatMap(accountVoucher -> fakeVouchers
                        .stream()
                        .filter(voucher -> voucher.getId().equals(accountVoucher.getVoucherId()))
                        .map(Voucher::getCode)
                )
                .toList();

        Double points = fakePoints
                .stream()
                .filter(point -> point.getAccountId().equals(authenticatedAccount.getId()))
                .map(Point::getFixedAmount)
                .reduce(0.0, Double::sum);

        TransactionCheckoutRequest request = TransactionCheckoutRequest
                .builder()
                .eventId(event.getId())
                .transactionTickets(transactionTickets)
                .voucherCodes(voucherCodes)
                .points(points)
                .build();

        webTestClient
                .post()
                .uri("/transactions/try-checkout")
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<TransactionCheckoutResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals("Checkout succeed.");
                    assert body.getData() != null;
                    assert body.getData().getId() != null;
                    assert body.getData().getEventId().equals(event.getId());
                    assert body.getData().getTransactionTickets().size() == transactionTickets.size();
                    assert voucherCodes.containsAll(body.getData().getVoucherCodes());
                    assert body.getData().getPoints() <= points;
                    assert body.getData().getFinalPrice() >= 0.0;
                });
    }
}