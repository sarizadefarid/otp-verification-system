package com.backend.Controller;

import com.backend.Service.OtpService;
import com.backend.dto.OtpGenerateRequest;
import com.backend.dto.OtpRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.backend.Service.EmailService;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    private final OtpService otpService;
    private final RateLimitService rateLimitService;
    private final EmailService emailService;

    public OtpController(OtpService otpService, RateLimitService rateLimitService, EmailService emailService) {
        this.otpService = otpService;
        this.rateLimitService = rateLimitService;
        this.emailService = emailService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateOtp(@RequestBody @Valid OtpGenerateRequest otpGenerateRequest) {
        rateLimitService.checkLimit(otpGenerateRequest.getEmail());
        otpService.generateAndSaveOtp(
                otpGenerateRequest.getEmail());

        return ResponseEntity.ok(
                "OTP sent successfully. Please check your email.");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody @Valid OtpRequest otpRequest) {
        String result = otpService.verifyOtp(otpRequest.getEmail(), otpRequest.getOtp());
        return ResponseEntity.ok(result);
    }

    @RestControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(RateLimitExceededException.class)
        public ResponseEntity<String> handleRateLimit(
                RateLimitExceededException exception) {

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(exception.getMessage());
        }
    }
}
