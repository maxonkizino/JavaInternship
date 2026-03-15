package com.javainternship.integration;

import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class PaymentCardIntegrationTest extends AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_DRIVER", () -> "org.postgresql.Driver");
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USERNAME", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
    }

    private String usersUrl() {
        return "/api/users";
    }

    private String cardsUrl() {
        return "/api/payment-cards";
    }

    @Test
    void fullFlow_create_get_update_activate_deactivate_delete_card() {
        // 1. Create user first
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setName("John");
        userRequest.setSurname("Doe");
        userRequest.setBirthDate(LocalDate.of(1990, 5, 15));
        userRequest.setEmail("john.card@example.com");

        EntityExchangeResult<UserResponse> userResult = webTestClient.post()
                .uri(usersUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .returnResult();

        UserResponse user = userResult.getResponseBody();
        assertThat(user).isNotNull();
        Long userId = user.getId();

        // 2. Create card for user
        CreatePaymentCardRequest cardRequest = new CreatePaymentCardRequest();
        cardRequest.setUserId(userId);
        cardRequest.setNumber("4111111111111111");
        cardRequest.setHolder("John Doe");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(3));
        cardRequest.setActive(true);

        EntityExchangeResult<PaymentCardResponse> createResult = webTestClient.post()
                .uri(cardsUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cardRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PaymentCardResponse.class)
                .returnResult();

        PaymentCardResponse created = createResult.getResponseBody();
        assertThat(created).isNotNull();
        Long cardId = created.getId();
        assertThat(created.getNumber()).isEqualTo("4111111111111111");

        // 3. Get card by id
        EntityExchangeResult<PaymentCardResponse> getResult = webTestClient.get()
                .uri(cardsUrl() + "/" + cardId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentCardResponse.class)
                .returnResult();
        assertThat(getResult.getResponseBody()).isNotNull();

        // 4. Get cards by user id
        EntityExchangeResult<List> byUserResult = webTestClient.get()
                .uri(cardsUrl() + "/by-user/" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult();
        assertThat(byUserResult.getResponseBody()).isNotNull();
        assertThat(byUserResult.getResponseBody()).isNotEmpty();

        // 5. Update card
        UpdatePaymentCardRequest updateRequest = new UpdatePaymentCardRequest();
        updateRequest.setHolder("Jane Doe");

        EntityExchangeResult<PaymentCardResponse> updateResult = webTestClient.put()
                .uri(cardsUrl() + "/" + cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentCardResponse.class)
                .returnResult();
        assertThat(updateResult.getResponseBody()).isNotNull();

        // 6. Deactivate
        webTestClient.post()
                .uri(cardsUrl() + "/" + cardId + "/deactivate")
                .exchange()
                .expectStatus().isNoContent();

        // 7. Activate
        webTestClient.post()
                .uri(cardsUrl() + "/" + cardId + "/activate")
                .exchange()
                .expectStatus().isNoContent();

        // 8. Delete card
        webTestClient.delete()
                .uri(cardsUrl() + "/" + cardId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
