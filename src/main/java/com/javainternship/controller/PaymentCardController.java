package com.javainternship.controller;

import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.update.SetPaymentCardStatusRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-cards")
@AllArgsConstructor
public class PaymentCardController {

    private final PaymentCardService service;

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponse>> getAllPaymentCards(@RequestParam(required = false) String name,
                                                                              @RequestParam(required = false) String surname,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "10") int size) {
        Page<PaymentCardResponse> result = service.searchPaymentCards(name, surname, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponse> getPaymentCardById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findPaymentCardById(id));
    }

    @GetMapping("/by-number/{cardNumber}")
    public ResponseEntity<PaymentCardResponse> getPaymentCardByNumber(@PathVariable String cardNumber) {
        return ResponseEntity.ok(service.findPaymentCardByCardNumber(cardNumber));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Page<PaymentCardResponse>> getCardsByUserId(@PathVariable Long userId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.findCardsByUserId(userId, PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<PaymentCardResponse> createPaymentCard(@Valid @RequestBody CreatePaymentCardRequest request) {
        PaymentCardResponse response = service.createPaymentCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponse> updatePaymentCard(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdatePaymentCardRequest request) {
        PaymentCardResponse response = service.updatePaymentCard(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id) {
        service.deletePaymentCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> setPaymentCardStatus(@PathVariable Long id,
                                                       @Valid @RequestBody SetPaymentCardStatusRequest request) {
        if (Boolean.TRUE.equals(request.getActive())) {
            service.activatePaymentCard(id);
        } else {
            service.deactivatePaymentCard(id);
        }
        return ResponseEntity.noContent().build();
    }
}
