package com.library.common.service;

import com.library.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig config;

    public String save(MultipartFile file, String folder) {

        if (file.isEmpty()) {
            throw new RuntimeException("File trống");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || !originalName.toLowerCase().matches(".*\\.(png|jpg|jpeg)$")) {
            throw new RuntimeException("Định dạng file không hợp lệ");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File quá lớn (max 5MB)");
        }


        String ext = "";

        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID() + ext;

        Path uploadPath = Paths.get(config.getUploadDir(), folder);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Lưu file thất bại", e);
        }

        String basePath = config.getAccessPath().endsWith("/")
                ? config.getAccessPath()
                : config.getAccessPath() + "/";

        return basePath + folder + "/" + fileName;
    }
}
