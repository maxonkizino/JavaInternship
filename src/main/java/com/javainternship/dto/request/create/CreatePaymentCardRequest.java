package com.javainternship.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreatePaymentCardRequest {



    @NotBlank
    @Size(min = 2, max = 255)
    private String number;

    @NotBlank
    @Size(min = 2, max = 255)
    private String holder;

    @NotNull
    private LocalDate expirationDate;

    @NotBlank
    @Size(min = 2, max = 255)
    private boolean active;


}
