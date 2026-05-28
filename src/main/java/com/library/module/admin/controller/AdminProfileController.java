package com.library.module.admin.controller;

import com.library.module.category.entity.Category;
import com.library.module.category.service.CategoryService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import com.library.util.CommonUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.library.module.address.service.VietnamAddressService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private VietnamAddressService vietnamAddressService;

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

    @GetMapping("/add-admin")
    public String loadAddAdmin(Model model) {
        model.addAttribute("adminForm", new User());
        return "/admin/add_admin";
    }

    @PostMapping("/save-admin")
    public String saveAdmin(@ModelAttribute("adminForm") User adminForm,
                            @RequestParam("img") MultipartFile file,
                            @RequestParam("cpassword") String confirmPassword,
                            HttpSession session) {
        try {
            if (adminForm.getPassword() == null || adminForm.getPassword().isBlank()) {
                session.setAttribute("errorMsg", "Mật khẩu không được để trống");
                return "redirect:/admin/add-admin";
            }

            if (!adminForm.getPassword().equals(confirmPassword)) {
                session.setAttribute("errorMsg", "Mật khẩu không khớp");
                return "redirect:/admin/add-admin";
            }

            if (!vietnamAddressService.isValidProvinceAndWard(adminForm.getCity(), adminForm.getState())) {
                session.setAttribute("errorMsg", "Địa chỉ không hợp lệ. Vui lòng chọn tỉnh/thành phố và xã/phường từ danh sách.");
                return "redirect:/admin/add-admin";
            }

            User savedUser = userService.saveAdmin(adminForm, file);

            if (!ObjectUtils.isEmpty(savedUser)) {
                session.setAttribute("succMsg", "Tạo admin thành công");
                return "redirect:/admin/users?type=2";
            }

            session.setAttribute("errorMsg", "Lỗi máy chủ");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/add-admin";
    }

    @GetMapping("/profile")
    public String profile() {
        return "/admin/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User user,
                                @RequestParam MultipartFile img,
                                Principal principal,
                                HttpSession session) {
        try {
            User loggedInUser = commonUtil.getLoggedInUserDetails(principal);

            if (loggedInUser == null) {
                return "redirect:/signin";
            }

            user.setId(loggedInUser.getId());

            if (!vietnamAddressService.isValidProvinceAndWard(user.getCity(), user.getState())) {
                session.setAttribute("errorMsg", "Địa chỉ không hợp lệ. Vui lòng chọn tỉnh/thành phố và xã/phường từ danh sách.");
                return "redirect:/admin/profile";
            }

            User updatedUser = userService.updateUserProfile(user, img);

            if (ObjectUtils.isEmpty(updatedUser)) {
                session.setAttribute("errorMsg", "Có lỗi xảy ra trong quá trình cập nhật thông tin");
            } else {
                session.setAttribute("succMsg", "Cập nhật thông tin thành công");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,
                                 @RequestParam String currentPassword,
                                 Principal principal,
                                 HttpSession session) {
        try {
            User loggedInUser = commonUtil.getLoggedInUserDetails(principal);

            if (loggedInUser == null) {
                return "redirect:/signin";
            }

            User updatedUser = userService.changePassword(
                    loggedInUser.getId(),
                    currentPassword,
                    newPassword
            );

            if (ObjectUtils.isEmpty(updatedUser)) {
                session.setAttribute("errorMsg", "Có lỗi xảy ra trong quá trình thay đổi mật khẩu");
            } else {
                session.setAttribute("succMsg", "Thay đổi mật khẩu thành công");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/profile";
    }
}
