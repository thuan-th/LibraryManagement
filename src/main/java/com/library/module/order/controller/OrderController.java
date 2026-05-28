package com.library.module.order.controller;

import com.library.module.cart.entity.CartItem;
import com.library.module.cart.service.CartService;
import com.library.module.order.dto.OrderRequest;
import com.library.module.order.entity.BookOrder;
import com.library.module.order.enums.OrderStatus;
import com.library.module.order.service.OrderService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import com.library.module.address.service.VietnamAddressService;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private VietnamAddressService vietnamAddressService;

    private User getUser(Principal p) {
        if (p == null) return null;
        return userService.getUserByEmail(p.getName());
    }

    @GetMapping("/checkout")
    public String checkout(Principal p, Model m) {

        User user = getUser(p);

        if (user == null) {
            return "redirect:/signin";
        }

        List<CartItem> items = cartService.getCartItems(user.getId());

        if (items == null || items.isEmpty()) {
            return "redirect:/user/cart";
        }

        int orderPrice = items.stream().mapToInt(CartItem::getTotalPrice).sum();
        int totalOrderPrice = orderPrice + 20000;

        m.addAttribute("items", items);
        m.addAttribute("orderPrice", orderPrice);
        m.addAttribute("totalOrderPrice", totalOrderPrice);
        m.addAttribute("user", user);

        return "user/order";
    }

    @PostMapping("/place")
    public String placeOrder(@ModelAttribute OrderRequest request,
                             Principal p,
                             HttpSession session) {
        try {
            User user = getUser(p);

            if (user == null) {
                return "redirect:/signin";
            }

            if (!vietnamAddressService.isValidProvinceAndWard(request.getCity(), request.getDistrict())) {
                session.setAttribute("errorMsg", "Địa chỉ không hợp lệ. Vui lòng chọn tỉnh/thành phố và xã/phường từ danh sách.");
                return "redirect:/order/checkout";
            }

            BookOrder order = orderService.saveOrder(user, request);

            if ("vnpay".equalsIgnoreCase(request.getPaymentType())) {
                return "redirect:/payment/vnpay?orderId=" + order.getOrderId();
            }

            if ("COD".equalsIgnoreCase(request.getPaymentType())) {
                orderService.handleCodOrder(order);
                return "redirect:/order/success";
            }

            session.setAttribute("errorMsg", "Phương thức thanh toán không hợp lệ");
            return "redirect:/order/checkout";

        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
            return "redirect:/user/cart";
        }
    }

    @GetMapping("/success")
    public String success() {
        return "user/success";
    }

    @GetMapping("/fail")
    public String fail() {
        return "user/orderfail";
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam Integer id,
                              Principal p,
                              HttpSession session) {

        User user = getUser(p);
        BookOrder order = orderService.getOrderById(id);

        if (order == null) {
            session.setAttribute("errorMsg", "Đơn không tồn tại");
            return "redirect:/user/user-orders";
        }

        if (!order.getUser().getId().equals(user.getId())) {
            session.setAttribute("errorMsg", "Không có quyền");
            return "redirect:/user/user-orders";
        }

        if (!(order.getStatus().equals(OrderStatus.PENDING_PAYMENT.name()) ||
                order.getStatus().equals(OrderStatus.PROCESSING.name()))) {

            session.setAttribute("errorMsg", "Không thể huỷ đơn");
            return "redirect:/user/user-orders";
        }

        orderService.cancelOrder(order);
        session.setAttribute("succMsg", "Huỷ đơn thành công");
        return "redirect:/user/user-orders";
    }

}
