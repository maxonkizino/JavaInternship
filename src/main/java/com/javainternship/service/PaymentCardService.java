package com.javainternship.service;

import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentCardService {

    PaymentCardResponse findPaymentCardByCardNumber(String cardNumber);

    PaymentCardResponse findPaymentCardById(Long id);

    List<PaymentCardResponse> findAllPaymentCards();

    Page<PaymentCardResponse> searchPaymentCards(String name, String surname, Pageable pageable);

    List<PaymentCardResponse> findCardsByUserId(Long userId);

    PaymentCardResponse createPaymentCard(CreatePaymentCardRequest request);

    PaymentCardResponse updatePaymentCard(UpdatePaymentCardRequest request, Long id);

    void deletePaymentCard(Long id);

    void activatePaymentCard(Long id);

    void deactivatePaymentCard(Long id);
}

