package com.library.module.wishlist.controller;

import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import com.library.module.wishlist.entity.WishList;
import com.library.module.wishlist.service.WishListService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class WishListController {

    @Autowired
    private WishListService wishListService;

    @Autowired
    private UserService userService;

    private User getLoggedInUserDetails(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userService.getUserByEmail(principal.getName());
    }

    @PostMapping("/wishlist/add")
    public String addToWishList(@RequestParam Integer pid,
                                Principal principal,
                                HttpSession session) {
        User user = getLoggedInUserDetails(principal);

        if (user == null) {
            return "redirect:/signin";
        }

        WishList savedWishList = wishListService.saveWishList(pid, user.getId());

        if (ObjectUtils.isEmpty(savedWishList)) {
            session.setAttribute("errorMsg", "Thêm sách không thành công");
        } else {
            session.setAttribute("succMsg", "Đã thêm vào danh sách yêu thích");
        }

        return "redirect:/book/" + pid;
    }

    @GetMapping("/wishlist")
    public String loadWishListPage(Principal principal, Model model) {
        User user = getLoggedInUserDetails(principal);

        if (user == null) {
            return "redirect:/signin";
        }

        List<WishList> wishLists = wishListService.getWishListsByUser(user.getId());

        model.addAttribute("wishLists", wishLists);

        if (wishLists.isEmpty()) {
            model.addAttribute("emptyWishListMsg", "Danh sách yêu thích của bạn hiện tại đang trống.");
        }

        return "/user/wishlist";
    }

    @PostMapping("/wishlist/delete/{id}")
    public String deleteWishList(@PathVariable Integer id,
                                 Principal principal,
                                 HttpSession session) {
        User user = getLoggedInUserDetails(principal);

        if (user == null) {
            return "redirect:/signin";
        }

        Boolean deleted = wishListService.deleteWishList(id, user.getId());

        if (deleted) {
            session.setAttribute("succMsg", "Đã xóa sách khỏi danh sách yêu thích");
        } else {
            session.setAttribute("errorMsg", "Không tìm thấy sách trong danh sách yêu thích của bạn");
        }

        return "redirect:/user/wishlist";
    }
}
