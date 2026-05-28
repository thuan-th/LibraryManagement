package com.library.module.user.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import com.library.module.order.entity.BookOrder;
import com.library.module.category.entity.Category;
import com.library.module.user.entity.User;
import com.library.module.order.service.OrderService;
import com.library.module.cart.service.CartService;
import com.library.module.category.service.CategoryService;
import com.library.module.user.service.UserService;
import com.library.module.address.service.VietnamAddressService;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VietnamAddressService vietnamAddressService;

    @ModelAttribute
    public void getUserDetails(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            User userDtls = userService.getUserByEmail(email);

            if (userDtls != null) {
                m.addAttribute("user", userDtls);

                Integer countCart = cartService.getCountCart(userDtls.getId());
                m.addAttribute("countCart", countCart);
            }
        }

        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        m.addAttribute("categorys", allActiveCategory);
    }

    private User getLoggedInUserDetails(Principal p) {
        String email = p.getName();
        User userDtls = userService.getUserByEmail(email);
        return userDtls;
    }

    @GetMapping("/user-orders")
    public String myOrder(Model m, Principal p) {
        User loginUser = getLoggedInUserDetails(p);
        List<BookOrder> orders = orderService.getOrdersByUser(loginUser.getId());

        m.addAttribute("orders", orders);

        return "/user/my_orders";
    }

    @GetMapping("/profile")
    public String profile() {
        return "/user/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User user,
                                @RequestParam(value = "img", required = false) MultipartFile img,
                                Principal principal,
                                HttpSession session) {
        try {
            User loggedInUser = getLoggedInUserDetails(principal);

            if (loggedInUser == null) {
                return "redirect:/signin";
            }

            user.setId(loggedInUser.getId());

            if (!vietnamAddressService.isValidProvinceAndWard(user.getCity(), user.getState())) {
                session.setAttribute("errorMsg", "Địa chỉ không hợp lệ. Vui lòng chọn tỉnh/thành phố và xã/phường từ danh sách.");
                return "redirect:/user/profile";
            }

            User updateUserProfile = userService.updateUserProfile(user, img);

            if (ObjectUtils.isEmpty(updateUserProfile)) {
                session.setAttribute("errorMsg", "Thay đổi hồ sơ không thành công");
            } else {
                session.setAttribute("succMsg", "Hồ sơ của bạn đã được thay đổi");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,
                                 @RequestParam String currentPassword,
                                 Principal p,
                                 HttpSession session) {
        try {
            User loggedInUser = getLoggedInUserDetails(p);

            if (loggedInUser == null) {
                return "redirect:/signin";
            }

            User updatedUser = userService.changePassword(
                    loggedInUser.getId(),
                    currentPassword,
                    newPassword
            );

            if (ObjectUtils.isEmpty(updatedUser)) {
                session.setAttribute("errorMsg", "Mật khẩu chưa được cập nhật, lỗi máy chủ");
            } else {
                session.setAttribute("succMsg", "Cập nhật mật khẩu thành công");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/user/profile";
    }
}
