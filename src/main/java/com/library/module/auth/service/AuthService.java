package com.library.module.auth.service;

import com.library.module.auth.dto.RegisterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    void register(RegisterRequest request, MultipartFile file);

    boolean verifyOtp(String email, String otp);

    void resendRegisterOtp(String email);
}