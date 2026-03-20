package com.javainternship.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUserNotFound_returns404() {
        UserNotFoundException ex = new UserNotFoundException("no user");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users/1");

        var response = handler.handleUserNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("no user");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/1");
    }

    @Test
    void handlePaymentCardNotFound_returns404() {
        PaymentCardNotFoundException ex = new PaymentCardNotFoundException("no card");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/payment-cards/1");

        var response = handler.handlePaymentCardNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("no card");
        assertThat(response.getBody().getPath()).isEqualTo("/api/payment-cards/1");
    }

    @Test
    void handleConstraintViolation_emptySet_returnsDefaultMessage() {
        ConstraintViolationException ex = new ConstraintViolationException("msg", Collections.emptySet());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users");

        var response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Constraint violation");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users");
    }

    @Test
    void handleMaxCardsPerUser_returns400() {
        MaxCardsPerUserExceededException ex = new MaxCardsPerUserExceededException("limit");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/payment-cards");

        var response = handler.handleMaxCardsPerUser(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("limit");
        assertThat(response.getBody().getPath()).isEqualTo("/api/payment-cards");
    }

    @Test
    void handleOtherExceptions_returns500() {
        Exception ex = new RuntimeException("boom");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/unknown");

        var response = handler.handleOtherExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("boom");
        assertThat(response.getBody().getPath()).isEqualTo("/api/unknown");
    }

    @Test
    void handleValidationException_usesFirstFieldError() {
        @SuppressWarnings("unchecked")
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getField()).thenReturn("email");
        when(fieldError.getDefaultMessage()).thenReturn("must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users");

        var response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("email must not be blank");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users");
    }
}

