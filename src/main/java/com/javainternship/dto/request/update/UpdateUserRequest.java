package com.javainternship.dto.request.update;


import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateUserRequest {


    @Size(min = 2, max = 255)
    private String name;


    @Size(min = 2, max = 255)
    private String surname;

    private LocalDate birtDate;

    @Size(min = 2, max = 255)
    private String email;

}
