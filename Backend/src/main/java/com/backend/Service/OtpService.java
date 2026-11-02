package com.backend.Service;

import org.springframework.stereotype.Service;
import com.backend.Service.OtpGenerator;
import com.backend.Entity.OtpCode;
import com.backend.Entity.OtpStatus;
import com.backend.Service.OtpHashService;
import com.backend.Repository.OtpRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OtpService {
    private final OtpGenerator otpGenerator;
    private final OtpRepository otpRepository;
    private final OtpHashService otpHashService;
    private final EmailService emailService;
    public OtpService(OtpGenerator otpGenerator, OtpRepository otpRepository, OtpHashService otpHashService, EmailService emailService) {
        this.otpGenerator = otpGenerator;
        this.otpRepository = otpRepository;
        this.otpHashService = otpHashService;
        this.emailService = emailService;

    }

    @Transactional
    public void generateAndSaveOtp(String email) {

        List<OtpCode> activeOtps = otpRepository.findByEmailAndStatus(email, OtpStatus.ACTIVE);

        for (OtpCode oldOtp : activeOtps) {
            oldOtp.setStatus(OtpStatus.INVALIDATED);
        }

        String otp = otpGenerator.generateOtp();

        String hashedOtp = otpHashService.hashOtp(otp, email);

        OtpCode otpCode = new OtpCode();

        otpCode.setEmail(email);
        otpCode.setCodeHash(hashedOtp);
        otpCode.setStatus(OtpStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();

        otpCode.setCreatedAt(now);
        otpCode.setExpiresAt(now.plusMinutes(5));

        otpRepository.save(otpCode);

        emailService.sendOtpEmail(email, otp);

    }

    @Transactional
    public String verifyOtp(String email, String otp) {

        OtpCode otpCode = otpRepository
                .findByEmailAndStatus(email, OtpStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Active OTP not found"));

        LocalDateTime now = LocalDateTime.now();

        if (!now.isBefore(otpCode.getExpiresAt())) {
            otpCode.setStatus(OtpStatus.EXPIRED);
            throw new RuntimeException("OTP has expired");
        }

        boolean isValid = otpHashService.matches(
                otp,
                email,
                otpCode.getCodeHash());

        if (!isValid) {
            throw new RuntimeException("Invalid OTP");
        }

        otpCode.setStatus(OtpStatus.USED);
        otpCode.setVerifiedAt(now);
        otpRepository.save(otpCode);
        return "OTP verified successfully";
    }

}
