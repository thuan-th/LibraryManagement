package com.library.module.auth.controller;

import com.library.module.auth.dto.RegisterRequest;
import com.library.module.auth.dto.VerifyOtpRequest;
import com.library.module.auth.service.AuthService;
import com.library.module.address.service.VietnamAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VietnamAddressService vietnamAddressService;

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @ModelAttribute RegisterRequest request,
                                        BindingResult bindingResult,
                                        @RequestParam(value = "img", required = false) MultipartFile file) {

        Map<String, Object> res = new HashMap<>();

        if (bindingResult.hasErrors()) {
            res.put("success", false);
            res.put("message", getFirstValidationMessage(bindingResult));
            return res;
        }

        if (!vietnamAddressService.isValidProvinceAndWard(request.getCity(), request.getState())) {
            res.put("success", false);
            res.put("message", "Địa chỉ không hợp lệ. Vui lòng chọn tỉnh/thành phố và xã/phường từ danh sách.");
            return res;
        }

        if (!request.getPassword().equals(request.getCpassword())) {
            res.put("success", false);
            res.put("message", "Mật khẩu không khớp");
            return res;
        }

        try {
            authService.register(request, file);

            res.put("success", true);
            res.put("message", "Đăng ký thành công");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }

        return res;
    }

    @PostMapping("/register/resend-otp")
    public Map<String, Object> resendRegisterOtp(@RequestBody Map<String, String> body) {
        Map<String, Object> res = new HashMap<>();

        try {
            String email = body.get("email");

            authService.resendRegisterOtp(email);

            res.put("success", true);
            res.put("message", "Mã OTP mới đã được gửi đến email của bạn");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }

        return res;
    }

    @PostMapping("/verify")
    public String verifyOtp(@ModelAttribute VerifyOtpRequest request) {

        boolean valid = authService.verifyOtp(request.getEmail(), request.getOtp());

        if (valid) {
            return "redirect:/signin?success";
        }

        return "redirect:/verify?error";
    }

    @PostMapping("/verify-otp")
    public Map<String, Object> verifyOtp(@RequestBody Map<String, String> body) {

        Map<String, Object> res = new HashMap<>();

        boolean valid = authService.verifyOtp(body.get("email"), body.get("otp"));

        res.put("success", valid);

        if (!valid) {
            res.put("message", "OTP không đúng");
        }

        return res;
    }

    private String getFirstValidationMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Dữ liệu đăng ký không hợp lệ");
    }
}