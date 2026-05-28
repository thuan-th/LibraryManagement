package com.library.module.feedback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String userEmail;

    @Column(length = 1000)
    private String content;

    private LocalDateTime submittedAt;

    private boolean isDisplayed;
}
