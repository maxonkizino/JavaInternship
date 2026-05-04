package com.javainternship.controller;

import com.javainternship.config.UserGatewayProperties;
import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.logging.ControllerLogger;
import com.javainternship.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Server-to-server registration used only by API Gateway (header {@code X-Gateway-Internal}).
 */
@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalGatewayUserController {

    private static final String GATEWAY_HEADER = "X-Gateway-Internal";

    private final UserGatewayProperties gatewayProperties;
    private final UserService userService;
    private final ControllerLogger controllerLogger;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestHeader(GATEWAY_HEADER) String secret,
            @Valid @RequestBody CreateUserRequest request) {
        controllerLogger.methodCalled("InternalGatewayUserController", "register", request.getEmail());
        if (!isValidSecret(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserResponse created = userService.createUserThroughGateway(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/register/{userId}/rollback")
    public ResponseEntity<Void> rollback(
            @RequestHeader(GATEWAY_HEADER) String secret,
            @PathVariable Long userId) {
        controllerLogger.methodCalled("InternalGatewayUserController", "rollback", userId);
        if (!isValidSecret(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.rollbackGatewayRegistration(userId);
        return ResponseEntity.noContent().build();
    }

    private boolean isValidSecret(String secret) {
        String expected = gatewayProperties.getInternalSecret();
        return expected != null && !expected.isBlank() && expected.equals(secret);
    }
}
