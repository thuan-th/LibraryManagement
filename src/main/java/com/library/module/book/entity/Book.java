package com.library.module.book.entity;

import com.library.module.cart.entity.CartItem;
import com.library.module.order.entity.BookOrderItem;
import com.library.module.comment.entity.Comment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 500)
    private String bookName;

    @Column(length = 5000)
    private String description;
    private String author;
    private String category;
    private String publisher;
    private Integer price;
    private Integer stock;
    private String image;
    private Integer discount;
    private Integer discountPrice;
    private String isbn;
    private Boolean isActive;
    private LocalDateTime createdDate;
    private int sold = 0;
    private double averageRating;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookOrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    public void calculateDiscountPrice() {
        if (price != null && discount != null && discount >= 0 && discount <= 100) {
            this.discountPrice = price - (price * discount / 100);
        } else {
            this.discountPrice = this.price;
        }
    }
}
