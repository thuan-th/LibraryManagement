package com.library.module.payment.service;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {

    String createVNPayUrl(String orderId, int amount, HttpServletRequest request);

    boolean verifyVNPay(HttpServletRequest request);
}