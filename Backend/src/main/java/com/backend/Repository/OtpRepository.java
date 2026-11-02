package com.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.Entity.OtpCode;
import com.backend.Entity.OtpStatus;

public interface OtpRepository extends JpaRepository<OtpCode, Long> {
    List<OtpCode> findByEmailAndStatus(String email, OtpStatus status);
}
