package com.javainternship.mapper;


import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.model.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    public PaymentCard toPaymentCard(CreatePaymentCardRequest request);

    public void toPaymentCard(UpdatePaymentCardRequest request, @MappingTarget PaymentCard paymentCard);

    public PaymentCardResponse toPaymentCardResponse(PaymentCard paymentCard);

    public List<PaymentCardResponse> toPaymentCardResponses(List<PaymentCard> paymentCards);

}
