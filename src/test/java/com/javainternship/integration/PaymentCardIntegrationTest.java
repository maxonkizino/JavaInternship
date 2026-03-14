package com.javainternship.integration;

import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.JavaInternshipApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JavaInternshipApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PaymentCardIntegrationTest {

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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String usersUrl() {
        return "http://localhost:" + port + "/api/users";
    }

    private String cardsUrl() {
        return "http://localhost:" + port + "/api/payment-cards";
    }

    @Test
    void fullFlow_create_get_update_activate_deactivate_delete_card() {
        // 1. Create user first
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setName("John");
        userRequest.setSurname("Doe");
        userRequest.setBirtDate(LocalDate.of(1990, 5, 15));
        userRequest.setEmail("john.card@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<UserResponse> userResponse =
                restTemplate.postForEntity(usersUrl(), new HttpEntity<>(userRequest, headers), UserResponse.class);

        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponse user = userResponse.getBody();
        assertThat(user).isNotNull();
        Long userId = user.getId();

        // 2. Create card for user
        CreatePaymentCardRequest cardRequest = new CreatePaymentCardRequest();
        cardRequest.setUserId(userId);
        cardRequest.setNumber("4111111111111111");
        cardRequest.setHolder("John Doe");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(3));
        cardRequest.setActive(true);

        ResponseEntity<PaymentCardResponse> createResponse =
                restTemplate.postForEntity(cardsUrl(), new HttpEntity<>(cardRequest, headers), PaymentCardResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PaymentCardResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        Long cardId = created.getId();
        assertThat(created.getNumber()).isEqualTo("4111111111111111");

        // 3. Get card by id
        ResponseEntity<PaymentCardResponse> getResponse =
                restTemplate.getForEntity(cardsUrl() + "/" + cardId, PaymentCardResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();

        // 4. Get cards by user id
        ResponseEntity<List> byUserResponse =
                restTemplate.getForEntity(cardsUrl() + "/by-user/" + userId, List.class);
        assertThat(byUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byUserResponse.getBody()).isNotEmpty();

        // 5. Update card
        UpdatePaymentCardRequest updateRequest = new UpdatePaymentCardRequest();
        updateRequest.setHolder("Jane Doe");
        ResponseEntity<PaymentCardResponse> updateResponse =
                restTemplate.exchange(cardsUrl() + "/" + cardId, HttpMethod.PUT,
                        new HttpEntity<>(updateRequest, headers), PaymentCardResponse.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();

        // 6. Deactivate
        ResponseEntity<Void> deactivateResponse =
                restTemplate.postForEntity(cardsUrl() + "/" + cardId + "/deactivate", null, Void.class);
        assertThat(deactivateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 7. Activate
        ResponseEntity<Void> activateResponse =
                restTemplate.postForEntity(cardsUrl() + "/" + cardId + "/activate", null, Void.class);
        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 8. Delete card
        ResponseEntity<Void> deleteResponse =
                restTemplate.exchange(cardsUrl() + "/" + cardId, HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
