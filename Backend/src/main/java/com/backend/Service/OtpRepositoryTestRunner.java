package com.backend.Service;

import org.springframework.boot.CommandLineRunner;
import com.backend.Entity.OtpCode;
import com.backend.Repository.OtpRepository;

public class OtpRepositoryTestRunner implements CommandLineRunner {

    private final OtpRepository otpRepository;

    public OtpRepositoryTestRunner(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        OtpCode otpCode = new OtpCode();
        OtpCode savedOtp = otpRepository.save(otpCode);

        System.out.println("Yaradılmış ID: " + savedOtp.getId());

        otpRepository.findById(savedOtp.getId())
                .ifPresent(System.out::println);
    }
}