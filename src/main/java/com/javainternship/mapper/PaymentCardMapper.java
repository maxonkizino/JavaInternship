package com.javainternship.mapper;


import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.model.PaymentCard;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(target = "user", ignore = true)
    PaymentCard toPaymentCard(CreatePaymentCardRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public void toPaymentCard(UpdatePaymentCardRequest request, @MappingTarget PaymentCard paymentCard);

    public PaymentCardResponse toPaymentCardResponse(PaymentCard paymentCard);

    public List<PaymentCardResponse> toPaymentCardResponses(List<PaymentCard> paymentCards);

}
