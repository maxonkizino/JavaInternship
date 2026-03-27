package com.javainternship.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePaymentCardRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 2, max = 255)
    private String number;

    @NotBlank
    @Size(min = 2, max = 255)
    private String holder;

    @NotNull
    private LocalDate expirationDate;

    private boolean active = true;

}
