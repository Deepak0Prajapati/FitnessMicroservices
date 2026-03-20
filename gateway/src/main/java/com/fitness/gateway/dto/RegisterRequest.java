package com.fitness.gateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required!")
    @Email(message = "Invalid Email format!!")
    private String email;
    private String keycloakId;
    @NotBlank(message = "Password is Required")
    @Size(min = 6, message = "password must have at least 6 characters")
    private String password;
    private String firstName;
    private String lastName;

}
