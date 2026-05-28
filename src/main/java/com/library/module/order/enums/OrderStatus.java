package com.library.module.order.enums;

public enum OrderStatus {
    PENDING_PAYMENT(0, "Chờ thanh toán"),
    PROCESSING(1, "Đang chờ xử lí"),
    ORDER_RECEIVED(2, "Xác nhận đơn"),
    PRODUCT_PACKED(3, "Đã đóng gói"),
    OUT_FOR_DELIVERY(4, "Đang vận chuyển"),
    DELIVERED(5, "Giao hàng thành công"),
    CANCELLED(6,"Đã hủy");

    private Integer id;

    private String name;

    OrderStatus(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static OrderStatus fromId(Integer id) {
        for (OrderStatus s : values()) {
            if (s.id.equals(id)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus id: " + id);
    }
}
