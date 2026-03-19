package com.javainternship.integration;

import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class UserIntegrationTest extends AbstractIntegrationTest {

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

    private String baseUrl() {
        return "/api/users";
    }

    @Test
    void fullFlow_create_get_update_activate_deactivate_delete_user() {
        CreateUserRequest create = new CreateUserRequest();
        create.setName("John");
        create.setSurname("Doe");
        create.setBirthDate(LocalDate.of(1990, 5, 15));
        create.setEmail("john.doe@example.com");

        EntityExchangeResult<UserResponse> createResult = webTestClient.post()
                .uri(baseUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(create)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .returnResult();

        UserResponse created = createResult.getResponseBody();
        assertThat(created).isNotNull();
        Long id = created.getId();
        assertThat(created.getName()).isEqualTo("John");
        assertThat(created.getEmail()).isEqualTo("john.doe@example.com");

        EntityExchangeResult<UserResponse> getResult = webTestClient.get()
                .uri(baseUrl() + "/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .returnResult();
        assertThat(getResult.getResponseBody()).isNotNull();

        UpdateUserRequest update = new UpdateUserRequest();
        update.setName("Jane");
        update.setSurname("Smith");

        EntityExchangeResult<UserResponse> updateResult = webTestClient.put()
                .uri(baseUrl() + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .returnResult();

        UserResponse updated = updateResult.getResponseBody();
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Jane");

        webTestClient.patch()
                .uri(baseUrl() + "/" + id + "/deactivate")
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.patch()
                .uri(baseUrl() + "/" + id + "/activate")
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.delete()
                .uri(baseUrl() + "/" + id)
                .exchange()
                .expectStatus().isNoContent();
    }
}
