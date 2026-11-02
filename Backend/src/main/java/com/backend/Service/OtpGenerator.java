package com.backend.Service;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.time.LocalDateTime;
@Component
public class OtpGenerator {
    private final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        // Generate a random 6-digit OTP
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

}