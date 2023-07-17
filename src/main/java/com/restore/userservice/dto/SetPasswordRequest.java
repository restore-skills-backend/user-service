package com.restore.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class SetPasswordRequest {

    @NotBlank(message = "new password is mandatory")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password requirements not met")
    private String newPassword;

    private UUID linkId;

    private UUID uuid;

    private UUID providerGroupId;
}
