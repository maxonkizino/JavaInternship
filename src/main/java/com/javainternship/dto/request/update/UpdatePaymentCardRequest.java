package com.javainternship.dto.request.update;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePaymentCardRequest {

    @Size(min = 2, max = 255)
    private String number;

    @Size(min = 2, max = 255)
    private String holder;

    private LocalDate expirationDate;

    private Boolean active;

}
