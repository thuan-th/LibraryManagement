package com.library.module.blog.service.impl;

import com.library.module.blog.service.BlogImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class BlogImageStorageServiceImpl implements BlogImageStorageService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public String storeThumbnail(MultipartFile file, String currentImage) throws IOException {
        if (file == null || file.isEmpty()) {
            return currentImage;
        }

        return store(file, "blog/thumbnail");
    }

    @Override
    public String storeContentImage(MultipartFile file) throws IOException {
        String fileName = store(file, "blog/content");
        return "/uploads/blog/content/" + fileName;
    }

    private String store(MultipartFile file, String folder) throws IOException {
        validateImage(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get(uploadPath).resolve(folder).normalize();
        Files.createDirectories(uploadDir);

        Path targetPath = uploadDir.resolve(fileName).normalize();

        if (!targetPath.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Đường dẫn upload ảnh không hợp lệ.");
        }

        Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh.");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Ảnh không được vượt quá 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Chỉ cho phép upload ảnh JPG, PNG hoặc WEBP.");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );

        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Tên file ảnh không hợp lệ.");
        }

        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Định dạng ảnh không hợp lệ.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("File ảnh phải có phần mở rộng.");
        }

        return filename.substring(filename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }
}
