package com.library.module.payment.controller;

import com.library.module.order.entity.BookOrder;
import com.library.module.order.enums.PaymentStatus;
import com.library.module.order.service.OrderService;
import com.library.module.payment.entity.PaymentTransaction;
import com.library.module.payment.repository.PaymentTransactionRepository;
import com.library.module.payment.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.library.module.order.enums.OrderStatus;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

import java.time.LocalDateTime;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentTransactionRepository paymentRepo;

    @Autowired
    private UserService userService;

    private User getLoggedInUser(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userService.getUserByEmail(principal.getName());
    }

    @GetMapping("/payment/vnpay")
    public String pay(@RequestParam String orderId,
                      HttpServletRequest request,
                      Principal principal) {
        User user = getLoggedInUser(principal);

        if (user == null) {
            return "redirect:/signin";
        }

        BookOrder order = orderService.getOrdersByOrderId(orderId);

        if (order == null) {
            return "redirect:/order/fail";
        }

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            return "redirect:/order/fail";
        }

        if (!"vnpay".equalsIgnoreCase(order.getPaymentType())) {
            return "redirect:/order/fail";
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            return "redirect:/order/fail";
        }

        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
            return "redirect:/order/fail";
        }

        String url = paymentService.createVNPayUrl(order.getOrderId(), order.getTotalAmount(), request);

        return "redirect:" + url;
    }

    @GetMapping("/payment/vnpay-return")
    public String vnpayReturn(HttpServletRequest request) {
        String orderId = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        BookOrder order = orderService.getOrdersByOrderId(orderId);

        if (order == null) {
            return "redirect:/order/fail";
        }

        PaymentTransaction trans = new PaymentTransaction();
        trans.setOrder(order);
        trans.setVnpTxnRef(orderId);
        trans.setVnpTransactionNo(request.getParameter("vnp_TransactionNo"));
        trans.setVnpResponseCode(responseCode);
        trans.setVnpAmount(request.getParameter("vnp_Amount"));
        trans.setCreatedAt(LocalDateTime.now());

        boolean validSignature = paymentService.verifyVNPay(request);
        boolean validAmount = isValidAmount(request.getParameter("vnp_Amount"), order.getTotalAmount());

        if (!validSignature || !validAmount) {
            trans.setStatus("INVALID");
            paymentRepo.save(trans);
            return "redirect:/order/fail";
        }

        if ("00".equals(responseCode)) {
            trans.setStatus("SUCCESS");
            orderService.handlePaymentSuccess(order);
            paymentRepo.save(trans);
            return "redirect:/order/success";
        }

        trans.setStatus("FAILED");

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            orderService.handlePaymentFail(order);
        }

        paymentRepo.save(trans);

        return "redirect:/order/fail";
    }

    private boolean isValidAmount(String vnpAmount, Integer orderAmount) {
        if (vnpAmount == null || orderAmount == null) {
            return false;
        }

        try {
            long paidAmount = Long.parseLong(vnpAmount);
            return paidAmount == orderAmount.longValue() * 100L;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}