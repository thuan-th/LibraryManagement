package com.library.module.payment.service.impl;

import com.library.module.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.url}")
    private String vnp_Url;


    @Override
    public String createVNPayUrl(String orderId, int amount, HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");

        String returnUrl = request.getScheme() + "://" + request.getServerName() + ":"
                + request.getServerPort() + "/payment/vnpay-return";

        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", request.getRemoteAddr());

        params.put("vnp_CreateDate", new java.text.SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));

        // sort params
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);

            hashData.append(fieldName).append('=').append(encodedValue).append('&');

            query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                    .append('=')
                    .append(encodedValue)
                    .append('&');
        }

        hashData.deleteCharAt(hashData.length() - 1);
        query.deleteCharAt(query.length() - 1);

        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHashType=HmacSHA512");
        return vnp_Url + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

//    @Override
//    public boolean verifyVNPay(HttpServletRequest request) {
//        return true;
//    }

    @Override
    public boolean verifyVNPay(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        String receivedHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);

            if (value != null && !value.isBlank()) {
                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                        .append('&');
            }
        }

        if (hashData.length() == 0) {
            return false;
        }

        hashData.deleteCharAt(hashData.length() - 1);

        String calculatedHash = hmacSHA512(vnp_HashSecret, hashData.toString());

        return calculatedHash.equalsIgnoreCase(receivedHash);
    }


    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}