package com.javainternship.dto.response;

import java.time.LocalDate;

public class PaymentCardResponse {

    private Long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private boolean active;

}
