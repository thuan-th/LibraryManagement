package com.library.module.payment.entity;

import com.library.module.order.entity.BookOrder;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vnpTxnRef;
    private String vnpTransactionNo;
    private String vnpResponseCode;
    private String vnpAmount;

    private String status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private BookOrder order;
}