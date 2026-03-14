package com.javainternship.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {

    private Long id;
    private String name;
    private String surname;
    private LocalDate birtDate;
    private String email;
    private boolean active;

}
