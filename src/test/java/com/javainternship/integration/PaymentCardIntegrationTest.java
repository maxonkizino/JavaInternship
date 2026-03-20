package com.javainternship.integration;

import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.request.update.SetPaymentCardStatusRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class PaymentCardIntegrationTest extends AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_DRIVER", () -> "org.postgresql.Driver");
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USERNAME", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    private String usersUrl() {
        return "/api/users";
    }

    private String cardsUrl() {
        return "/api/payment-cards";
    }

    @Test
    void fullFlow_create_get_update_activate_deactivate_delete_card() {
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

        EntityExchangeResult<PaymentCardResponse> getResult = webTestClient.get()
                .uri(cardsUrl() + "/" + cardId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentCardResponse.class)
                .returnResult();
        assertThat(getResult.getResponseBody()).isNotNull();

        EntityExchangeResult<Map> byUserResult = webTestClient.get()
                .uri(cardsUrl() + "/by-user/" + userId + "?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();
        assertThat(byUserResult.getResponseBody()).isNotNull();
        List<?> content = (List<?>) byUserResult.getResponseBody().get("content");
        assertThat(content).isNotNull().isNotEmpty();

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

        SetPaymentCardStatusRequest deactivateReq = new SetPaymentCardStatusRequest();
        deactivateReq.setActive(false);

        webTestClient.patch()
                .uri(cardsUrl() + "/" + cardId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deactivateReq)
                .exchange()
                .expectStatus().isNoContent();

        SetPaymentCardStatusRequest activateReq = new SetPaymentCardStatusRequest();
        activateReq.setActive(true);

        webTestClient.patch()
                .uri(cardsUrl() + "/" + cardId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(activateReq)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.delete()
                .uri(cardsUrl() + "/" + cardId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(cardsUrl() + "/" + cardId)
                .exchange()
                .expectStatus().isNotFound();
    }
}
