package com.library.module.order.enums;

public enum PaymentStatus {

    PENDING("Chờ thanh toán"),
    PAID("Đã thanh toán"),
    FAILED("Thanh toán thất bại");

    private final String name;

    PaymentStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PaymentStatus fromString(String status) {
        for (PaymentStatus s : PaymentStatus.values()) {
            if (s.name().equalsIgnoreCase(status) || s.getName().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentStatus: " + status);
    }
}