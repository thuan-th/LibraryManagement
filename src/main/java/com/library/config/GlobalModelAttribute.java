package com.library.config;

import com.library.module.cart.service.CartService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAttribute {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @ModelAttribute("user")
    public User getUser(Principal principal) {

        if (principal == null) return null;

        return userService.getUserByEmail(principal.getName());
    }

    @ModelAttribute("countCart")
    public Integer getCartCount(Principal principal) {

        if (principal == null) return 0;

        User user = userService.getUserByEmail(principal.getName());

        if (user == null) {
            return 0;
        }

        if (!"ROLE_USER".equals(user.getRole())) {
            return 0;
        }

        return cartService.getCartItems(user.getId()).size();
    }
}