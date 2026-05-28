package com.library.module.auth.controller;

import com.library.module.auth.dto.ForgotPasswordRequest;
import com.library.module.auth.dto.ResetPasswordRequest;
import com.library.module.auth.service.OtpService;
import com.library.module.user.entity.User;
import com.library.module.user.enums.AuthProvider;
import com.library.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ForgotPasswordController {

    private static final long RESET_TOKEN_EXPIRY_TIME = 10 * 60 * 1000;

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/api/auth/forgot-password")
    public Map<String, Object> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Map<String, Object> res = new HashMap<>();

        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            res.put("success", false);
            res.put("message", "Email không tồn tại");
            return res;
        }

        if (isGoogleAccount(user)) {
            res.put("success", false);
            res.put("message", "Tài khoản này đăng nhập bằng Google, vui lòng sử dụng Google để quản lý mật khẩu.");
            return res;
        }

        try {
            otpService.sendOtp(request.getEmail());

            res.put("success", true);
            res.put("message", "OTP đã được gửi đến email của bạn");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }

        return res;
    }

    @PostMapping("/api/auth/verify-otp")
    public Map<String, Object> verifyOtp(@RequestBody Map<String, String> body) {
        Map<String, Object> res = new HashMap<>();

        String email = body.get("email");
        String otp = body.get("otp");

        User user = userRepository.findByEmail(email);

        if (user == null) {
            res.put("success", false);
            res.put("message", "Email không tồn tại");
            return res;
        }

        if (isGoogleAccount(user)) {
            res.put("success", false);
            res.put("message", "Tài khoản này đăng nhập bằng Google, vui lòng sử dụng Google để quản lý mật khẩu.");
            return res;
        }

        try {
            boolean valid = otpService.verifyOtp(email, otp);

            if (!valid) {
                res.put("success", false);
                res.put("message", "OTP không đúng");
                return res;
            }

            String resetToken = UUID.randomUUID().toString();

            user.setResetToken(resetToken);
            user.setResetTokenExpiry(new Date(System.currentTimeMillis() + RESET_TOKEN_EXPIRY_TIME));

            userRepository.save(user);

            res.put("success", true);
            res.put("resetToken", resetToken);
            res.put("message", "Xác thực OTP thành công");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }

        return res;
    }

    @PostMapping("/api/auth/reset-password")
    public Map<String, Object> resetPassword(@RequestBody ResetPasswordRequest request) {
        Map<String, Object> res = new HashMap<>();

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            res.put("success", false);
            res.put("message", "Email không hợp lệ");
            return res;
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            res.put("success", false);
            res.put("message", "Mật khẩu không được để trống");
            return res;
        }

        if (request.getResetToken() == null || request.getResetToken().isBlank()) {
            res.put("success", false);
            res.put("message", "Phiên đổi mật khẩu không hợp lệ");
            return res;
        }

        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            res.put("success", false);
            res.put("message", "Email không tồn tại");
            return res;
        }

        if (isGoogleAccount(user)) {
            res.put("success", false);
            res.put("message", "Tài khoản này đăng nhập bằng Google, vui lòng sử dụng Google để quản lý mật khẩu.");
            return res;
        }

        if (user.getResetToken() == null || !user.getResetToken().equals(request.getResetToken())) {
            res.put("success", false);
            res.put("message", "Bạn chưa xác thực OTP hoặc phiên đổi mật khẩu không hợp lệ");
            return res;
        }

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().before(new Date())) {
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);

            res.put("success", false);
            res.put("message", "Phiên đổi mật khẩu đã hết hạn, vui lòng yêu cầu OTP mới");
            return res;
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        user.setAccountNonLocked(true);

        userRepository.save(user);

        res.put("success", true);
        res.put("message", "Đổi mật khẩu thành công");

        return res;
    }

    private boolean isGoogleAccount(User user) {
        return user.getAuthProvider() != null && user.getAuthProvider() == AuthProvider.GOOGLE;
    }
}