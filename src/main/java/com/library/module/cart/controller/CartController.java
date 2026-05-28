package com.library.module.cart.controller;

import com.library.module.cart.entity.CartItem;
import com.library.module.cart.service.CartService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/cart")
public class CartController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    private User getUser(Principal p) {
        if (p == null) {
            return null;
        }

        return userService.getUserByEmail(p.getName());
    }

    @GetMapping
    public String viewCart(Principal p, Model m) {

        if (p == null) {
            return "redirect:/signin";
        }

        User user = getUser(p);

        List<CartItem> items = cartService.getCartItems(user.getId());
        m.addAttribute("items", items);

        if (items.isEmpty()) {
            m.addAttribute("emptyCartMsg", "Giỏ hàng trống");
        } else {
            int total = items.stream()
                    .mapToInt(CartItem::getTotalPrice)
                    .sum();

            m.addAttribute("totalOrderPrice", total);
        }

        return "user/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCartAjax(
            @RequestParam Integer pid,
            @RequestParam(defaultValue = "1") Integer quantity,
            Principal p) {

        Map<String, Object> res = new HashMap<>();

        try {
            if (p == null) {
                res.put("success", false);
                res.put("message", "Bạn chưa đăng nhập");
                return res;
            }

            User user = getUser(p);

            cartService.addToCart(user.getId(), pid, quantity);

            res.put("success", true);
            res.put("message", "Thêm vào giỏ hàng thành công");

        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }

        return res;
    }

    @PostMapping("/update-ajax")
    @ResponseBody
    public Map<String, Object> updateCartAjax(
            @RequestParam Integer itemId,
            @RequestParam String sy,
            Principal p) {

        Map<String, Object> res = new HashMap<>();

        try {
            if (p == null) {
                res.put("success", false);
                res.put("message", "Bạn chưa đăng nhập");
                return res;
            }

            User user = getUser(p);

            cartService.updateQuantity(sy, itemId, user.getId());

            CartItem item = cartService.getItemById(itemId, user.getId());

            int cartTotal = cartService.getCartItems(user.getId())
                    .stream().mapToInt(CartItem::getTotalPrice).sum();

            if (item == null) {
                res.put("success", true);
                res.put("deleted", true);
                res.put("cartTotal", cartTotal);
                return res;
            }

            res.put("success", true);
            res.put("deleted", false);
            res.put("quantity", item.getQuantity());
            res.put("totalPrice", item.getTotalPrice());
            res.put("cartTotal", cartTotal);

        } catch (Exception e) {
            e.printStackTrace();
//            res.put("success", true);
//            res.put("deleted", true);
            res.put("success", false);
            res.put("message", e.getMessage());
        }

        return res;
    }

    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getCartCount(Principal p) {
        Map<String, Object> res = new HashMap<>();

        if (p == null) {
            res.put("count", 0);
            return res;
        }

        User user = getUser(p);
        int count = cartService.getCountCart(user.getId());

        res.put("count", count);
        return res;
    }
}
