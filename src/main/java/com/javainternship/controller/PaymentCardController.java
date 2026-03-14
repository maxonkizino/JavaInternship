package com.javainternship.controller;

import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.service.interf.PaymentCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-cards")
@AllArgsConstructor
public class PaymentCardController {

    private final PaymentCardService service;

    @GetMapping
    public ResponseEntity<List<PaymentCardResponse>> getAllPaymentCards() {
        return ResponseEntity.ok(service.findAllPaymentCards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponse> getPaymentCardById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findPaymentCardById(id));
    }

    @GetMapping("/by-number")
    public ResponseEntity<PaymentCardResponse> getPaymentCardByNumber(@RequestParam String cardNumber) {
        return ResponseEntity.ok(service.findPaymentCardByCardNumber(cardNumber));
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
}
