package com.javainternship.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCardResponse {

    private Long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private boolean active;

}
