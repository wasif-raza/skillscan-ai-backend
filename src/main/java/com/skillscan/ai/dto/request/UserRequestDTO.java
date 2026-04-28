package com.skillscan.ai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequestDTO {

    @NotBlank(message = "First Name is required")
    private String firstName;

    private String lastNAme;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
}
