package com.library.module.order.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.library.module.order.enums.PaymentStatus;
import com.library.module.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class BookOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderId;

    private LocalDateTime orderDate;

    @ManyToOne
    private User user;

    private String status;

    private String paymentType;

    private Integer totalAmount;

    @OneToOne(cascade = CascadeType.ALL)
    private OrderAddress orderAddress;

    @OneToMany(mappedBy = "bookOrder", cascade = CascadeType.ALL)
    private List<BookOrderItem> items;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

}
