package com.library.module.auth.service.impl;

import com.library.module.auth.entity.Otp;
import com.library.module.auth.repository.OtpRepository;
import com.library.module.auth.service.OtpService;
import com.library.common.service.AsyncMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final AsyncMailService asyncMailService;

    @Override
    public void sendOtp(String email) {

        Otp lastOtp = otpRepository.findTopByEmailOrderByIdDesc(email);

        if (lastOtp != null &&
                lastOtp.getExpiryTime().minusMinutes(4).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Vui lòng chờ trước khi gửi lại OTP");
        }

        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtp(otpCode);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otp.setAttempts(0);

        otpRepository.save(otp);

        asyncMailService.sendOtpMail(email, otpCode);
    }

    @Override
    public boolean verifyOtp(String email, String otpInput) {

        Otp otp = otpRepository.findTopByEmailOrderByIdDesc(email);

        if (otp == null) return false;

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn");
        }

        if (otp.getAttempts() >= 5) {
            throw new RuntimeException("Quá số lần thử");
        }

        otp.setAttempts(otp.getAttempts() + 1);
        otpRepository.save(otp);

        return otp.getOtp().equals(otpInput);
    }
}