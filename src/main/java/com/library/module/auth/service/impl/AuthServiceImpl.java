package com.library.module.auth.service.impl;

import com.library.module.auth.dto.RegisterRequest;
import com.library.module.auth.service.AuthService;
import com.library.module.auth.service.OtpService;
import com.library.module.user.entity.User;
import com.library.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_AVATAR = "default.jpg";
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    @Transactional
    public void register(RegisterRequest request, MultipartFile file) {
        if (!request.getPassword().equals(request.getCpassword())) {
            throw new RuntimeException("Mật khẩu không khớp");
        }

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email đã tồn tại");
        }

        String fileName = storeUserAvatar(file, DEFAULT_AVATAR);

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName().trim());
        user.setMobileNumber(request.getMobileNumber().trim());
        user.setAddress(request.getAddress().trim());
        user.setCity(request.getCity().trim());
        user.setState(request.getState().trim());
        user.setProfileImage(fileName);
        user.setIsEnable(false);
        user.setRole("ROLE_USER");
        user.setAccountNonLocked(false);
        user.setFailedAttempt(0);
        user.setEmailVerified(false);

        userRepository.save(user);

        try {
            otpService.sendOtp(email);
        } catch (Exception e) {
            deleteUploadedAvatarIfNeeded(fileName);
            throw new RuntimeException("Tạo tài khoản thành công nhưng gửi OTP thất bại. Vui lòng thử lại sau.", e);
        }
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        boolean valid = otpService.verifyOtp(email, otp);

        if (!valid) {
            return false;
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User không tồn tại: " + email);
        }

        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        userRepository.save(user);

        return true;
    }

    private String storeUserAvatar(MultipartFile file, String currentAvatar) {
        if (file == null || file.isEmpty()) {
            return currentAvatar;
        }

        validateAvatar(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;

        Path uploadDir = Paths.get(uploadPath, "users");

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path targetPath = uploadDir.resolve(fileName).normalize();

            if (!targetPath.startsWith(uploadDir.normalize())) {
                throw new RuntimeException("Đường dẫn upload avatar không hợp lệ");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh thất bại", e);
        }
    }

    private void validateAvatar(MultipartFile file) {
        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new RuntimeException("Tên file không hợp lệ");
        }

        String extension = getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh .jpg, .jpeg, .png, .webp");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new RuntimeException("File ảnh không được vượt quá 5MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh .jpg, .jpeg, .png, .webp");
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName == null ? -1 : fileName.lastIndexOf(".");

        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new RuntimeException("File ảnh không có phần mở rộng hợp lệ");
        }

        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void deleteUploadedAvatarIfNeeded(String fileName) {
        if (fileName == null || fileName.isBlank() || DEFAULT_AVATAR.equals(fileName)) {
            return;
        }

        try {
            Path uploadDir = Paths.get(uploadPath, "users").normalize();
            Path filePath = uploadDir.resolve(fileName).normalize();

            if (filePath.startsWith(uploadDir)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void resendRegisterOtp(String email) {
        if (email == null || email.trim().isBlank()) {
            throw new RuntimeException("Email không hợp lệ");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail);

        if (user == null) {
            throw new RuntimeException("Tài khoản chưa được đăng ký");
        }

        if (Boolean.TRUE.equals(user.getIsEnable())) {
            throw new RuntimeException("Tài khoản đã được kích hoạt, vui lòng đăng nhập");
        }

        otpService.sendOtp(normalizedEmail);
    }
}
