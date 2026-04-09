package com.prajjwal.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDto {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank
    @Size(min = 8, message = "Password must at least 8 characters")
    private String newPassword;
}