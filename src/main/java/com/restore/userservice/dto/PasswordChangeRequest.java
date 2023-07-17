package com.restore.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeRequest {

    @NotBlank(message = "old password is mandatory")
    private String oldPassword;

    @NotBlank(message = "new password is mandatory")
    private String newPassword;

}
