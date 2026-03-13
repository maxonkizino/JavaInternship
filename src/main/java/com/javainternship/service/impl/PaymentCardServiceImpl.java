package com.javainternship.service.impl;


import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.exception.PaymentCardNotFoundException;
import com.javainternship.mapper.PaymentCardMapper;
import com.javainternship.model.PaymentCard;
import com.javainternship.repository.PaymentCardRepository;
import com.javainternship.service.interf.PaymentCardService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    public PaymentCardRepository repository;
    public PaymentCardMapper mapper;



    @Override
    public PaymentCardResponse findPaymentCardByCardNumber(String cardNumber) {
        PaymentCard card = repository.findByNumber(cardNumber)
                .orElseThrow(()->new PaymentCardNotFoundException("Payment Card Not Found with email:"+cardNumber));
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    public PaymentCardResponse findPaymentCardById(Long id) {
        PaymentCard card = repository.findById(id).orElseThrow(()->new PaymentCardNotFoundException("Payment Card Not Found with id:"+id));
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    public List<PaymentCardResponse> findAllPaymentCards() {
        List<PaymentCard> cards = repository.findAll();
        return mapper.toPaymentCardResponses(cards);
    }

    @Override
    @Transactional
    public PaymentCardResponse createPaymentCard(CreatePaymentCardRequest request) {
        PaymentCard card = mapper.toPaymentCard(request);
        card = repository.save(card);
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    @Transactional
    public PaymentCardResponse updatePaymentCard(UpdatePaymentCardRequest request, Long id) {
        PaymentCard card = repository.findById(id).orElseThrow(()->new PaymentCardNotFoundException("Payment Card Not Found with id:"+id));
        mapper.toPaymentCard(request,card);
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    @Transactional
    public void deletePaymentCard(Long id) {
        repository.deleteById(id);
    }
}
