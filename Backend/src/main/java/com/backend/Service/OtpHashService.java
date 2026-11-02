package com.backend.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OtpHashService {

    private final String secret;

    public OtpHashService(
            @Value("${otp.hmac-secret}") String secret) {
        this.secret = secret;
    }

    public String hashOtp(String otp, String email) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");

            mac.init(key);

            byte[] hash = mac.doFinal(
                    (email + ":" + otp).getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException("OTP hash yaradılmadı", e);
        }
    }

    public boolean matches(String otp, String email, String storedHash) {
        String calculatedHash = hashOtp(otp, email);

        return MessageDigest.isEqual(
                calculatedHash.getBytes(StandardCharsets.US_ASCII),
                storedHash.getBytes(StandardCharsets.US_ASCII));
    }
}