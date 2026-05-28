package com.library.module.auth.repository;

import com.library.module.auth.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Otp findTopByEmailOrderByIdDesc(String email);
}