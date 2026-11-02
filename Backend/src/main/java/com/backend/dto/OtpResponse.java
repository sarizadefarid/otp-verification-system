package com.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpResponse {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;
}
