package com.library.module.user.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.library.module.user.entity.User;
import com.library.module.user.repository.UserRepository;
import com.library.module.user.service.UserService;
import com.library.util.AppConstant;
import com.library.module.user.enums.AuthProvider;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String DEFAULT_AVATAR = "default.jpg";
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${upload.path}")
    private String uploadPath;


    @Override
    public User saveUser(User user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(false);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        User saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getUsers(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {

        Optional<User> findByuser = userRepository.findById(id);

        if (findByuser.isPresent()) {
            User userDtls = findByuser.get();
            userDtls.setIsEnable(status);
            userRepository.save(userDtls);
            return true;
        }

        return false;
    }

    @Override
    public void increaseFailedAttempt(User user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void userAccountLock(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public boolean unlockAccountTimeExpired(User user) {

        long lockTime = user.getLockTime().getTime();
        long unLockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;

        long currentTime = System.currentTimeMillis();

        if (unLockTime < currentTime) {
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void resetAttempt(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Email không tồn tại");
        }

        user.setResetToken(resetToken);
        userRepository.save(user);
    }

    @Override
    public User getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUserProfile(User user, MultipartFile file) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        if (user.getName() == null || !user.getName().trim().matches("^[A-Za-zÀ-ỹ\\s]+$")) {
            throw new RuntimeException("Tên không hợp lệ");
        }

        dbUser.setName(user.getName().trim());

        if (user.getMobileNumber() == null || !user.getMobileNumber().trim().matches("^[0-9]{10}$")) {
            throw new RuntimeException("SĐT không hợp lệ");
        }

        dbUser.setMobileNumber(user.getMobileNumber().trim());
        dbUser.setAddress(user.getAddress());
        dbUser.setCity(user.getCity());
        dbUser.setState(user.getState());

        String imageName = storeUserAvatar(file, dbUser.getProfileImage());

        dbUser.setProfileImage(imageName);

        return userRepository.save(dbUser);
    }

    private String storeUserAvatar(MultipartFile file, String currentAvatar) {
        if (file == null || file.isEmpty()) {
            return currentAvatar == null || currentAvatar.isBlank() ? DEFAULT_AVATAR : currentAvatar;
        }

        validateAvatar(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;

        Path uploadDir = Paths.get(uploadPath).resolve("users").normalize();

        try {
            Files.createDirectories(uploadDir);

            Path targetPath = uploadDir.resolve(fileName).normalize();

            if (!targetPath.startsWith(uploadDir)) {
                throw new RuntimeException("Đường dẫn upload avatar không hợp lệ");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Upload avatar thất bại", e);
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

    @Override
    public User saveAdmin(User user, MultipartFile file) {
        user.setId(null);

        if (user.getName() == null || user.getName().isBlank()) {
            throw new RuntimeException("Họ tên không được để trống");
        }

        String name = user.getName().trim();

        if (!name.matches("^[\\p{L}\\s]+$")) {
            throw new RuntimeException("Họ tên chỉ được chứa chữ cái và khoảng trắng");
        }

        user.setName(name);

        if (user.getMobileNumber() == null || user.getMobileNumber().isBlank()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        String mobileNumber = user.getMobileNumber().trim();

        if (!mobileNumber.matches("^[0-9]+$")) {
            throw new RuntimeException("Số điện thoại chỉ được chứa chữ số");
        }

        if (!mobileNumber.matches("^[0-9]{10}$")) {
            throw new RuntimeException("Số điện thoại phải có đúng 10 chữ số");
        }

        user.setMobileNumber(mobileNumber);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("Email không được để trống");
        }

        User existingUser = userRepository.findByEmail(user.getEmail().trim());

        if (existingUser != null) {
            throw new RuntimeException("Email đã tồn tại");
        }

        user.setEmail(user.getEmail().trim());

        user.setRole("ROLE_ADMIN");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        String imageName = storeUserAvatar(file, DEFAULT_AVATAR);
        user.setProfileImage(imageName);

        return userRepository.save(user);
    }

    @Override
    public User changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        if (user.getAuthProvider() != null && user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new RuntimeException("Tài khoản đăng nhập bằng Google không thể đổi mật khẩu trong hồ sơ");
        }

        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedAttempt(0);
        user.setLockTime(null);
        user.setAccountNonLocked(true);

        return userRepository.save(user);
    }

}
