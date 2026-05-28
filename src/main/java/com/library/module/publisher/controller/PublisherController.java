package com.library.module.publisher.controller;

import com.library.module.publisher.entity.Publisher;
import com.library.module.publisher.service.PublisherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class PublisherController {

    private static final String DEFAULT_IMAGE = "default.jpg";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Autowired
    private PublisherService publisherService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/publisher")
    public String publisher(Model model,
                            @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<Publisher> page = publisherService.getAllPublisherPagination(pageNo, pageSize);

        model.addAttribute("publishers", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/publisher";
    }

    @PostMapping("/savePublisher")
    public String savePublisher(@ModelAttribute Publisher publisher,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                HttpSession session) {
        try {
            String publisherName = normalizePublisherName(publisher.getName());

            if (publisherName.isBlank()) {
                session.setAttribute("errorMsg", "Tên nhà xuất bản không được để trống");
                return "redirect:/admin/publisher";
            }

            if (publisherName.length() > 100) {
                session.setAttribute("errorMsg", "Tên nhà xuất bản không được vượt quá 100 ký tự");
                return "redirect:/admin/publisher";
            }

            if (publisherService.existPublisher(publisherName)) {
                session.setAttribute("errorMsg", "Tên nhà xuất bản đã tồn tại");
                return "redirect:/admin/publisher";
            }

            String imageName = storePublisherImage(file, DEFAULT_IMAGE);

            publisher.setName(publisherName);
            publisher.setImageName(imageName);

            if (publisher.getIsActive() == null) {
                publisher.setIsActive(true);
            }

            Publisher savedPublisher = publisherService.savePublisher(publisher);

            if (ObjectUtils.isEmpty(savedPublisher)) {
                session.setAttribute("errorMsg", "Lưu không thành công! Lỗi máy chủ");
            } else {
                session.setAttribute("succMsg", "Lưu thành công");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/publisher";
    }

    @PostMapping("/deletePublisher/{id}")
    public String deletePublisher(@PathVariable int id, HttpSession session) {
        Boolean deleted = publisherService.deletePublisher(id);

        if (deleted) {
            session.setAttribute("succMsg", "Nhà xuất bản đã được xóa thành công");
        } else {
            session.setAttribute("errorMsg", "Có lỗi xảy ra");
        }

        return "redirect:/admin/publisher";
    }

    @GetMapping("/loadEditPublisher/{id}")
    public String loadEditPublisher(@PathVariable int id, Model model, HttpSession session) {
        Publisher publisher = publisherService.getPublisherById(id);

        if (ObjectUtils.isEmpty(publisher)) {
            session.setAttribute("errorMsg", "Không tìm thấy nhà xuất bản");
            return "redirect:/admin/publisher";
        }

        model.addAttribute("publisher", publisher);
        return "admin/edit_publisher";
    }

    @PostMapping("/updatePublisher")
    public String updatePublisher(@ModelAttribute Publisher publisher,
                                  @RequestParam(value = "file", required = false) MultipartFile file,
                                  HttpSession session) {
        try {
            Publisher oldPublisher = publisherService.getPublisherById(publisher.getId());

            if (ObjectUtils.isEmpty(oldPublisher)) {
                session.setAttribute("errorMsg", "Không tìm thấy nhà xuất bản");
                return "redirect:/admin/publisher";
            }

            String publisherName = normalizePublisherName(publisher.getName());

            if (publisherName.isBlank()) {
                session.setAttribute("errorMsg", "Tên nhà xuất bản không được để trống");
                return "redirect:/admin/loadEditPublisher/" + publisher.getId();
            }

            if (publisherName.length() > 100) {
                session.setAttribute("errorMsg", "Tên nhà xuất bản không được vượt quá 100 ký tự");
                return "redirect:/admin/loadEditPublisher/" + publisher.getId();
            }

            if (publisherService.existPublisherByNameAndNotId(publisherName, publisher.getId())) {
                session.setAttribute("errorMsg", "Tên nhà xuất bản đã tồn tại");
                return "redirect:/admin/loadEditPublisher/" + publisher.getId();
            }

            String imageName = storePublisherImage(file, oldPublisher.getImageName());

            oldPublisher.setName(publisherName);
            oldPublisher.setIsActive(Boolean.TRUE.equals(publisher.getIsActive()));
            oldPublisher.setImageName(imageName);

            Publisher updatedPublisher = publisherService.savePublisher(oldPublisher);

            if (ObjectUtils.isEmpty(updatedPublisher)) {
                session.setAttribute("errorMsg", "Cập nhật nhà xuất bản không thành công");
            } else {
                session.setAttribute("succMsg", "Cập nhật nhà xuất bản thành công");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/loadEditPublisher/" + publisher.getId();
        }

        return "redirect:/admin/publisher";
    }

    private String normalizePublisherName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    private String storePublisherImage(MultipartFile file, String currentImageName) throws IOException {
        if (file == null || file.isEmpty()) {
            return ObjectUtils.isEmpty(currentImageName) ? DEFAULT_IMAGE : currentImageName;
        }

        validateImage(file);

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String imageName = UUID.randomUUID() + "." + extension;

        Path uploadDir = Paths.get(uploadPath, "publishers");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path imagePath = uploadDir.resolve(imageName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return imageName;
    }

    private void validateImage(MultipartFile file) {
        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new RuntimeException("Tên file không hợp lệ");
        }

        String extension = getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Ảnh nhà xuất bản chỉ được dùng định dạng JPG, JPEG, PNG hoặc WEBP");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException("Ảnh nhà xuất bản không được vượt quá 5MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("File tải lên phải là ảnh JPG, JPEG, PNG hoặc WEBP");
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new RuntimeException("File ảnh không có phần mở rộng hợp lệ");
        }

        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
