package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.*;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.authentications.LoginByEmailAndPasswordRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.miniproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.miniproject1backend1.outers.repositories.ones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

@SpringBootTest
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
public class TestConfiguration {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected AccountRepository accountRepository;
    @Autowired
    protected AccountVoucherRepository accountVoucherRepository;
    @Autowired
    protected EventRepository eventRepository;
    @Autowired
    protected EventTicketRepository eventTicketRepository;
    @Autowired
    protected EventTicketFieldRepository eventTicketFieldRepository;
    @Autowired
    protected EventVoucherRepository eventVoucherRepository;
    @Autowired
    protected PointRepository pointRepository;
    @Autowired
    protected TransactionRepository transactionRepository;
    @Autowired
    protected TransactionTicketFieldRepository transactionTicketFieldRepository;
    @Autowired
    protected TransactionVoucherRepository transactionVoucherRepository;
    @Autowired
    protected VoucherRepository voucherRepository;

    @Autowired
    protected SecurityConfiguration securityConfiguration;

    @Autowired
    @Qualifier("oneTemplate")
    protected R2dbcEntityTemplate oneTemplate;

    protected ArrayList<Account> fakeAccounts = new ArrayList<>();
    protected ArrayList<AccountVoucher> fakeAccountVouchers = new ArrayList<>();
    protected ArrayList<Event> fakeEvents = new ArrayList<>();
    protected ArrayList<EventTicket> fakeEventTickets = new ArrayList<>();
    protected ArrayList<EventTicketField> fakeEventTicketFields = new ArrayList<>();
    protected ArrayList<EventVoucher> fakeEventVouchers = new ArrayList<>();
    protected ArrayList<Point> fakePoints = new ArrayList<>();
    protected ArrayList<Transaction> fakeTransactions = new ArrayList<>();
    protected ArrayList<TransactionTicketField> fakeTransactionTicketFields = new ArrayList<>();
    protected ArrayList<TransactionVoucher> fakeTransactionTicketVouchers = new ArrayList<>();
    protected ArrayList<Voucher> fakeVouchers = new ArrayList<>();

    protected String rawPassword = String.format("password-%s", UUID.randomUUID());
    protected Account authenticatedAccount;
    protected Session authenticatedSession;

    public void configure() {
        this.webTestClient = this.webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void populate() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        for (int i = 0; i < 4; i++) {
            Account newAccount = Account
                    .builder()
                    .id(UUID.randomUUID())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .email(String.format("email-%s", UUID.randomUUID()))
                    .password(securityConfiguration.encode(rawPassword))
                    .phone(String.format("phone-%s", UUID.randomUUID()))
                    .dob(now)
                    .referralCode(String.format("referralCode-%s", UUID.randomUUID()))
                    .profileImageUrl(String.format("profileImageUrl-%s", UUID.randomUUID())) // Added profile image
                    .build();
            fakeAccounts.add(newAccount);
        }
        StepVerifier.create(accountRepository.saveAll(fakeAccounts)).expectNextCount(fakeAccounts.size()).verifyComplete();

        // Voucher, Account Voucher
        for (Account account : fakeAccounts) {
            Voucher voucher = Voucher
                    .builder()
                    .id(UUID.randomUUID())
                    .code(String.format("code-%s", UUID.randomUUID()))
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .description(String.format("description-%s", UUID.randomUUID()))
                    .variableAmount(10.0 + Math.random() * 90)
                    .startedAt(now.minusDays(1))
                    .endedAt(now.plusDays(1))
                    .build();
            fakeVouchers.add(voucher);

            AccountVoucher accountVoucher = AccountVoucher
                    .builder()
                    .id(UUID.randomUUID())
                    .accountId(account.getId())
                    .voucherId(voucher.getId())
                    .quantity(1 + (int) (Math.random() * 4))
                    .build();
            fakeAccountVouchers.add(accountVoucher);
        }
        StepVerifier.create(voucherRepository.saveAll(fakeVouchers)).expectNextCount(fakeVouchers.size()).verifyComplete();
        StepVerifier.create(accountVoucherRepository.saveAll(fakeAccountVouchers)).expectNextCount(fakeAccountVouchers.size()).verifyComplete();

        // Event, EventTicket, EventTicketField, EventVoucher
        for (Account account : fakeAccounts) {
            Event event = Event
                    .builder()
                    .id(UUID.randomUUID())
                    .accountId(account.getId())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .description(String.format("desc-%s", UUID.randomUUID()))
                    .location(String.format("location-%s", UUID.randomUUID()))
                    .category(String.format("category-%s", UUID.randomUUID()))
                    .time(now.plusDays(1))
                    .bannerImageUrl(String.format("bannerImageUrl-%s", UUID.randomUUID()))
                    .build();
            fakeEvents.add(event);

            EventTicket eventTicket = EventTicket
                    .builder()
                    .id(UUID.randomUUID())
                    .eventId(event.getId())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .description(String.format("desc-%s", UUID.randomUUID()))
                    .price(50.0 + Math.random() * 50)
                    .slots(50 + (int) (Math.random() * 50))
                    .build();
            fakeEventTickets.add(eventTicket);

            EventTicketField eventTicketField = EventTicketField
                    .builder()
                    .id(UUID.randomUUID())
                    .eventTicketId(eventTicket.getId())
                    .key(String.format("key-%s", UUID.randomUUID()))
                    .build();
            fakeEventTicketFields.add(eventTicketField);


            for (Voucher voucher : fakeVouchers) { // Associate some vouchers with events
                EventVoucher eventVoucher = EventVoucher
                        .builder()
                        .id(UUID.randomUUID())
                        .voucherId(voucher.getId())
                        .eventId(event.getId())
                        .build();
                fakeEventVouchers.add(eventVoucher);
            }
        }
        StepVerifier.create(eventRepository.saveAll(fakeEvents)).expectNextCount(fakeEvents.size()).verifyComplete();
        StepVerifier.create(eventTicketRepository.saveAll(fakeEventTickets)).expectNextCount(fakeEventTickets.size()).verifyComplete();
        StepVerifier.create(eventTicketFieldRepository.saveAll(fakeEventTicketFields)).expectNextCount(fakeEventTicketFields.size()).verifyComplete();
        StepVerifier.create(eventVoucherRepository.saveAll(fakeEventVouchers)).expectNextCount(fakeEventVouchers.size()).verifyComplete();

        // Point
        for (Account account : fakeAccounts) {
            Point point = Point
                    .builder()
                    .id(UUID.randomUUID())
                    .accountId(account.getId())
                    .fixedAmount(100.0 + Math.random() * 100)
                    .endedAt(now.plusDays(30))
                    .build();
            fakePoints.add(point);
        }
        StepVerifier.create(pointRepository.saveAll(fakePoints)).expectNextCount(fakePoints.size()).verifyComplete();

        // Transaction, TransactionTicketField, TransactionVoucher, TransactionPoint
        for (Event event : fakeEvents) {
            for (Account account : fakeAccounts) { // Transactions for different users for each event

                Transaction transaction = Transaction
                        .builder()
                        .id(UUID.randomUUID())
                        .accountId(account.getId())
                        .eventId(event.getId())
                        .time(now)
                        .build();
                fakeTransactions.add(transaction);

                for (EventTicketField eventTicketField : fakeEventTicketFields) {
                    if (eventTicketField.getEventTicketId().equals(fakeEventTickets.stream().filter(et -> et.getEventId().equals(event.getId())).findFirst().get().getId())) {
                        TransactionTicketField transactionTicketField = TransactionTicketField
                                .builder()
                                .id(UUID.randomUUID())
                                .transactionId(transaction.getId())
                                .eventTicketFieldId(eventTicketField.getId())
                                .value(String.format("value-%s", UUID.randomUUID()))
                                .build();
                        fakeTransactionTicketFields.add(transactionTicketField);
                    }
                }


                for (Voucher voucher : fakeVouchers) {
                    TransactionVoucher transactionVoucher = TransactionVoucher
                            .builder()
                            .id(UUID.randomUUID())
                            .transactionId(transaction.getId())
                            .voucherId(voucher.getId())
                            .quantity(1 + (int) (Math.random() * 2))
                            .build();

                    fakeTransactionTicketVouchers.add(transactionVoucher);
                }
            }
        }
        StepVerifier.create(transactionRepository.saveAll(fakeTransactions)).expectNextCount(fakeTransactions.size()).verifyComplete();
        StepVerifier.create(transactionTicketFieldRepository.saveAll(fakeTransactionTicketFields)).expectNextCount(fakeTransactionTicketFields.size()).verifyComplete();
        StepVerifier.create(transactionVoucherRepository.saveAll(fakeTransactionTicketVouchers)).expectNextCount(fakeTransactionTicketVouchers.size()).verifyComplete();
    }


    public void depopulate() {
        StepVerifier.create(transactionTicketFieldRepository.deleteAll(fakeTransactionTicketFields)).verifyComplete();
        StepVerifier.create(transactionVoucherRepository.deleteAll(fakeTransactionTicketVouchers)).verifyComplete();
        StepVerifier.create(transactionRepository.deleteAll(fakeTransactions)).verifyComplete();
        StepVerifier.create(pointRepository.deleteAll(fakePoints)).verifyComplete();
        StepVerifier.create(eventVoucherRepository.deleteAll(fakeEventVouchers)).verifyComplete();
        StepVerifier.create(eventTicketFieldRepository.deleteAll(fakeEventTicketFields)).verifyComplete();
        StepVerifier.create(eventTicketRepository.deleteAll(fakeEventTickets)).verifyComplete();
        StepVerifier.create(eventRepository.deleteAll(fakeEvents)).verifyComplete();
        StepVerifier.create(accountVoucherRepository.deleteAll(fakeAccountVouchers)).verifyComplete();
        StepVerifier.create(voucherRepository.deleteAll(fakeVouchers)).verifyComplete();
        StepVerifier.create(accountRepository.deleteAll(fakeAccounts)).verifyComplete();
    }

    public void auth() {
        authenticatedAccount = register().getData();
        fakeAccounts.add(authenticatedAccount);
        authenticatedSession = login(authenticatedAccount).getData();
        String authorization = String.format("Bearer %s", authenticatedSession.getAccessToken());
        webTestClient = webTestClient
                .mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build();
    }

    public void auth(Account account) {
        authenticatedAccount = account;
        authenticatedSession = login(account).getData();
        String authorization = String.format("Bearer %s", authenticatedSession.getAccessToken());
        webTestClient = webTestClient
                .mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build();
    }

    public void deauth() {
        logout(authenticatedSession);
    }

    protected ResponseBody<Account> register() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        RegisterByEmailAndPasswordRequest requestBody = RegisterByEmailAndPasswordRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(String.format("email-%s", UUID.randomUUID()))
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .dob(now)
                .referralCode(null)
                .build();

        return webTestClient
                .post()
                .uri("/authentications/registers/email-password")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Account>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals("Register succeed.");
                    assert body.getData() != null;
                    assert body.getData().getId() != null;
                    assert body.getData().getName().equals(requestBody.getName());
                    assert body.getData().getEmail().equals(requestBody.getEmail());
                    assert securityConfiguration.matches(requestBody.getPassword(), body.getData().getPassword());
                    assert body.getData().getPhone().equals(requestBody.getPhone());
                    assert body.getData().getDob().equals(requestBody.getDob());
                    assert body.getData().getReferralCode() != null;
                })
                .returnResult()
                .getResponseBody();
    }

    protected ResponseBody<Session> login(Account account) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LoginByEmailAndPasswordRequest requestBody = LoginByEmailAndPasswordRequest
                .builder()
                .email(account.getEmail())
                .password(rawPassword)
                .build();

        return webTestClient
                .post()
                .uri("/authentications/logins/email-password")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Session>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals("Login succeed.");
                    assert body.getData() != null;
                    assert body.getData().getAccessToken() != null;
                    assert body.getData().getRefreshToken() != null;
                    assert body.getData().getAccessTokenExpiredAt().isAfter(now);
                    assert body.getData().getRefreshTokenExpiredAt().isAfter(now);
                })
                .returnResult()
                .getResponseBody();
    }

    protected ResponseBody<Void> logout(Session session) {
        return this.webTestClient
                .post()
                .uri("/authentications/logouts/session")
                .bodyValue(session)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Void>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals("Logout succeed.");
                })
                .returnResult()
                .getResponseBody();
    }

}
