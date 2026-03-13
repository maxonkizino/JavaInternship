package com.javainternship.service.interf;


import com.javainternship.dto.request.create.CreatePaymentCardRequest;

import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.model.PaymentCard;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;


public interface PaymentCardService {

    PaymentCardResponse findPaymentCardByCardNumber(String cardNumber);

    PaymentCardResponse findPaymentCardById(Long id);

    List<PaymentCardResponse> findAllPaymentCards();






    PaymentCardResponse createPaymentCard(CreatePaymentCardRequest request);


    PaymentCardResponse updatePaymentCard(UpdatePaymentCardRequest request, Long id);


    void deletePaymentCard(Long id);




}
