package com.javainternship.integration;

import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JavaInternshipApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserIntegrationTest {

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

    private String baseUrl() {
        return "http://localhost:" + port + "/api/users";
    }

    @Test
    void fullFlow_create_get_update_activate_deactivate_delete_user() {
        CreateUserRequest create = new CreateUserRequest();
        create.setName("John");
        create.setSurname("Doe");
        create.setBirthDate(LocalDate.of(1990, 5, 15));
        create.setEmail("john.doe@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserRequest> requestEntity = new HttpEntity<>(create, headers);

        ResponseEntity<UserResponse> createResponse =
                restTemplate.postForEntity(baseUrl(), requestEntity, UserResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        Long id = created.getId();
        assertThat(created.getName()).isEqualTo("John");
        assertThat(created.getEmail()).isEqualTo("john.doe@example.com");

        // get by id
        ResponseEntity<UserResponse> getResponse =
                restTemplate.getForEntity(baseUrl() + "/" + id, UserResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();

        // update
        UpdateUserRequest update = new UpdateUserRequest();
        update.setName("Jane");
        update.setSurname("Smith");
        HttpEntity<UpdateUserRequest> updateEntity = new HttpEntity<>(update, headers);
        ResponseEntity<UserResponse> updateResponse =
                restTemplate.exchange(baseUrl() + "/" + id, HttpMethod.PUT, updateEntity, UserResponse.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getName()).isEqualTo("Jane");

        // deactivate
        ResponseEntity<Void> deactivateResponse =
                restTemplate.postForEntity(baseUrl() + "/" + id + "/deactivate", null, Void.class);
        assertThat(deactivateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // activate
        ResponseEntity<Void> activateResponse =
                restTemplate.postForEntity(baseUrl() + "/" + id + "/activate", null, Void.class);
        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // delete
        ResponseEntity<Void> deleteResponse =
                restTemplate.exchange(baseUrl() + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
