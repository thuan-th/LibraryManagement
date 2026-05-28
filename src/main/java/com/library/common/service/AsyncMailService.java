package com.library.common.service;

import com.library.module.order.entity.BookOrder;

public interface AsyncMailService {

    void sendOtpMail(String email, String otp);

    void sendOrderMail(BookOrder order, String status);
}