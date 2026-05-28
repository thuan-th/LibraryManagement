package com.library.module.admin.controller;

import com.library.module.category.entity.Category;
import com.library.module.category.service.CategoryService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
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
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class CategoryManagementController {

    private static final String DEFAULT_IMAGE = "default.jpg";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Value("${upload.path}")
    private String uploadPath;

    @ModelAttribute
    public void getUserDetails(Principal principal, Model model) {
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            if (user != null) {
                model.addAttribute("user", user);
            }
        }

        List<Category> activeCategories = categoryService.getAllActiveCategory();
        model.addAttribute("categorys", activeCategories);
    }

    @GetMapping("/category")
    public String category(Model model,
                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);

        model.addAttribute("categorys", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam(value = "file", required = false) MultipartFile file,
                               HttpSession session) {
        try {
            String categoryName = normalizeCategoryName(category.getName());

            if (categoryName.isBlank()) {
                session.setAttribute("errorMsg", "Tên danh mục không được để trống");
                return "redirect:/admin/category";
            }

            if (categoryName.length() > 100) {
                session.setAttribute("errorMsg", "Tên danh mục không được vượt quá 100 ký tự");
                return "redirect:/admin/category";
            }

            if (categoryService.existCategory(categoryName)) {
                session.setAttribute("errorMsg", "Tên danh mục đã tồn tại");
                return "redirect:/admin/category";
            }

            String imageName = storeCategoryImage(file, DEFAULT_IMAGE);

            category.setName(categoryName);
            category.setImageName(imageName);

            if (category.getIsActive() == null) {
                category.setIsActive(true);
            }

            Category savedCategory = categoryService.saveCategory(category);

            if (ObjectUtils.isEmpty(savedCategory)) {
                session.setAttribute("errorMsg", "Lưu không thành công");
            } else {
                session.setAttribute("succMsg", "Lưu thành công");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/category";
    }

    @PostMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {
        Boolean deleted = categoryService.deleteCategory(id);

        if (deleted) {
            session.setAttribute("succMsg", "Xóa danh mục thành công");
        } else {
            session.setAttribute("errorMsg", "Lỗi máy chủ");
        }

        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory(@PathVariable int id, Model model, HttpSession session) {
        Category category = categoryService.getCategoryById(id);

        if (ObjectUtils.isEmpty(category)) {
            session.setAttribute("errorMsg", "Không tìm thấy danh mục");
            return "redirect:/admin/category";
        }

        model.addAttribute("category", category);
        return "admin/edit_category";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 HttpSession session) {
        try {
            Category oldCategory = categoryService.getCategoryById(category.getId());

            if (ObjectUtils.isEmpty(oldCategory)) {
                session.setAttribute("errorMsg", "Không tìm thấy danh mục");
                return "redirect:/admin/category";
            }

            String categoryName = normalizeCategoryName(category.getName());

            if (categoryName.isBlank()) {
                session.setAttribute("errorMsg", "Tên danh mục không được để trống");
                return "redirect:/admin/loadEditCategory/" + category.getId();
            }

            if (categoryName.length() > 100) {
                session.setAttribute("errorMsg", "Tên danh mục không được vượt quá 100 ký tự");
                return "redirect:/admin/loadEditCategory/" + category.getId();
            }

            if (categoryService.existCategoryByNameAndNotId(categoryName, category.getId())) {
                session.setAttribute("errorMsg", "Tên danh mục đã tồn tại");
                return "redirect:/admin/loadEditCategory/" + category.getId();
            }

            String imageName = storeCategoryImage(file, oldCategory.getImageName());

            oldCategory.setName(categoryName);
            oldCategory.setIsActive(Boolean.TRUE.equals(category.getIsActive()));
            oldCategory.setImageName(imageName);

            Category updatedCategory = categoryService.saveCategory(oldCategory);

            if (ObjectUtils.isEmpty(updatedCategory)) {
                session.setAttribute("errorMsg", "Xảy ra lỗi trong quá trình cập nhật danh mục");
            } else {
                session.setAttribute("succMsg", "Cập nhật danh mục thành công");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/loadEditCategory/" + category.getId();
        }

        return "redirect:/admin/category";
    }

    private String normalizeCategoryName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    private String storeCategoryImage(MultipartFile file, String currentImageName) throws IOException {
        if (file == null || file.isEmpty()) {
            return ObjectUtils.isEmpty(currentImageName) ? DEFAULT_IMAGE : currentImageName;
        }

        validateImage(file);

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String imageName = UUID.randomUUID() + "." + extension;

        Path uploadDir = Paths.get(uploadPath, "categories");

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
            throw new RuntimeException("Ảnh danh mục chỉ được dùng định dạng JPG, JPEG, PNG hoặc WEBP");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException("Ảnh danh mục không được vượt quá 5MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new RuntimeException("File tải lên phải là ảnh");
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
