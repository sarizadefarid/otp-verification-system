package com.backend.otp_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.backend")
@EnableJpaRepositories(basePackages = "com.backend.Repository")
@EntityScan(basePackages = "com.backend.Entity")
public class OtpSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OtpSystemApplication.class, args);
	}
}