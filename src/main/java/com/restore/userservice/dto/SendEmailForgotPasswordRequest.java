package com.restore.userservice.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailForgotPasswordRequest {

    @NotBlank(message = "Email id is mandatory")
    @Email(message = "Invalid email format")
    private String email;

    private String subdomain;
}
