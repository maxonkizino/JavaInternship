package com.javainternship.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class CreateUserRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    private String name;


    @NotBlank
    @Size(min = 2, max = 255)
    private String surname;


    @NotNull
    private LocalDate birtDate;

    @NotBlank
    @Size(min = 2, max = 255)
    private String email;


}
