package com.javainternship.dto.request.update;


import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdatePaymentCardRequest {

    @Size(min = 2, max = 255)
    private String number;

    @Size(min = 2, max = 255)
    private String holder;

    private LocalDate expirationDate;

    @Size(min = 2, max = 255)
    private boolean active;

}
