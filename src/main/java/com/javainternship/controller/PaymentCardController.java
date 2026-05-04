package com.javainternship.controller;

import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.update.SetPaymentCardStatusRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.logging.ControllerLogger;
import com.javainternship.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-cards")
@AllArgsConstructor
public class PaymentCardController {

    private final PaymentCardService service;
    private final ControllerLogger controllerLogger;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<PaymentCardResponse>> getAllPaymentCards(@RequestParam(required = false) String name,
                                                                              @RequestParam(required = false) String surname,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "10") int size) {
        controllerLogger.methodCalled("PaymentCardController", "getAllPaymentCards", name, surname, page, size);
        Page<PaymentCardResponse> result = service.searchPaymentCards(name, surname, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentCardResponse> getPaymentCardById(@PathVariable Long id) {
        controllerLogger.methodCalled("PaymentCardController", "getPaymentCardById", id);
        return ResponseEntity.ok(service.findPaymentCardById(id));
    }

    @GetMapping("/by-number/{cardNumber}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentCardResponse> getPaymentCardByNumber(@PathVariable String cardNumber) {
        controllerLogger.methodCalled("PaymentCardController", "getPaymentCardByNumber", cardNumber);
        return ResponseEntity.ok(service.findPaymentCardByCardNumber(cardNumber));
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<PaymentCardResponse>> getCardsByUserId(@PathVariable Long userId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        controllerLogger.methodCalled("PaymentCardController", "getCardsByUserId", userId, page, size);
        return ResponseEntity.ok(service.findCardsByUserId(userId, PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentCardResponse> createPaymentCard(@Valid @RequestBody CreatePaymentCardRequest request) {
        controllerLogger.methodCalled("PaymentCardController", "createPaymentCard", request.getUserId());
        PaymentCardResponse response = service.createPaymentCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentCardResponse> updatePaymentCard(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdatePaymentCardRequest request) {
        controllerLogger.methodCalled("PaymentCardController", "updatePaymentCard", id);
        PaymentCardResponse response = service.updatePaymentCard(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id) {
        controllerLogger.methodCalled("PaymentCardController", "deletePaymentCard", id);
        service.deletePaymentCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Void> setPaymentCardStatus(@PathVariable Long id,
                                                       @Valid @RequestBody SetPaymentCardStatusRequest request) {
        controllerLogger.methodCalled("PaymentCardController", "setPaymentCardStatus", id, request.getActive());
        if (Boolean.TRUE.equals(request.getActive())) {
            service.activatePaymentCard(id);
        } else {
            service.deactivatePaymentCard(id);
        }
        return ResponseEntity.noContent().build();
    }
}
