package com.library.module.auth.service;

public interface OtpService {

    void sendOtp(String email);

    boolean verifyOtp(String email, String otp);
}