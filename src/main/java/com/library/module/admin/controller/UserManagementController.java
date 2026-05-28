package com.library.module.admin.controller;

import com.library.module.category.entity.Category;
import com.library.module.category.service.CategoryService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class UserManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

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

    @GetMapping("/users")
    public String getAllUsers(Model model, @RequestParam Integer type) {
        List<User> users;

        if (type == 1) {
            users = userService.getUsers("ROLE_USER");
        } else {
            users = userService.getUsers("ROLE_ADMIN");
        }

        model.addAttribute("userType", type);
        model.addAttribute("users", users);

        return "/admin/users";
    }

    @PostMapping("/updateSts")
    public String updateUserAccountStatus(@RequestParam Boolean status,
                                          @RequestParam Integer id,
                                          @RequestParam Integer type,
                                          HttpSession session) {
        Boolean updated = userService.updateAccountStatus(id, status);

        if (updated) {
            session.setAttribute("succMsg", "Đã cập nhật tài khoản");
        } else {
            session.setAttribute("errorMsg", "Đã xảy ra lỗi");
        }

        return "redirect:/admin/users?type=" + type;
    }
}
