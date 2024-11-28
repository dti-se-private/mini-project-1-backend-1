package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.authentications.LoginByEmailAndPasswordRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class AuthenticationRestTest extends TestConfiguration {

    @BeforeEach
    public void beforeEach() {
        configure();
        populate();
    }

    @AfterEach
    public void afterEach() {
        depopulate();
    }

    @Test
    public void testRegisterByEmailAndPasswordWithReferralCode() {
        Account referralOwnerAccount = fakeAccounts.getFirst();
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        RegisterByEmailAndPasswordRequest requestBody = RegisterByEmailAndPasswordRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(String.format("email-%s", UUID.randomUUID()))
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .dob(now)
                .referralCode(referralOwnerAccount.getReferralCode())
                .build();

        webTestClient
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
                    fakeAccounts.add(body.getData());
                });
    }


    @Test
    public void testRegisterByEmailAndPasswordWithNotFoundReferralCode() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        RegisterByEmailAndPasswordRequest requestBody = RegisterByEmailAndPasswordRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(String.format("email-%s", UUID.randomUUID()))
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .dob(now)
                .referralCode(String.format("referralCode-%s", UUID.randomUUID()))
                .build();

        webTestClient
                .post()
                .uri("/authentications/registers/email-password")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void testLoginByEmailAndPassword() {
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        LoginByEmailAndPasswordRequest requestBody = LoginByEmailAndPasswordRequest
                .builder()
                .email(realAccount.getEmail())
                .password(rawPassword)
                .build();

        webTestClient
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
                    assert body.getData().getAccessTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
                    assert body.getData().getRefreshTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
                });

        fakeAccounts.add(realAccount);
    }

    @Test
    public void testLogout() {
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        ResponseBody<Session> loginResponse = login(realAccount);
        Session requestBody = loginResponse.getData();

        webTestClient
                .post()
                .uri("/authentications/logouts/session")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Void>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals("Logout succeed.");
                });

        fakeAccounts.add(realAccount);
    }

    @Test
    public void testRefreshSession() {
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        ResponseBody<Session> loginResponse = login(realAccount);
        Session requestBody = loginResponse.getData();

        webTestClient
                .post()
                .uri("/authentications/refreshes/session")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Session>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().getAccessToken() != null;
                    assert body.getData().getRefreshToken() != null;
                    assert body.getData().getAccessTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
                    assert body.getData().getRefreshTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
                });

        fakeAccounts.add(realAccount);
    }
}
