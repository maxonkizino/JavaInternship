package com.javainternship.dto.request.update;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {


    @Size(min = 2, max = 255)
    private String name;


    @Size(min = 2, max = 255)
    private String surname;

    private LocalDate birthDate;

    @Size(min = 2, max = 255)
    private String email;

}
