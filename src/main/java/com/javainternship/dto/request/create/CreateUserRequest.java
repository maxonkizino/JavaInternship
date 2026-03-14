package com.javainternship.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    private String name;


    @NotBlank
    @Size(min = 2, max = 255)
    private String surname;


    @NotNull
    private LocalDate birthDate;

    @NotBlank
    @Size(min = 2, max = 255)
    private String email;


}
