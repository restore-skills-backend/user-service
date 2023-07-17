package com.restore.userservice.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ForgotPasswordEmailRequest {

    @NotBlank(message = "Email id is mandatory")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "name is mandatory")
    private String name;

    private String subdomain;

    private UUID uuid;

}
