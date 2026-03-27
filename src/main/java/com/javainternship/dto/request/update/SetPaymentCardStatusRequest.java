package com.javainternship.dto.request.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetPaymentCardStatusRequest {

    @NotNull
    private Boolean active;
}

