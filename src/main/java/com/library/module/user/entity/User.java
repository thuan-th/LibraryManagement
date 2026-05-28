package com.library.module.user.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.library.module.comment.entity.Comment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.library.module.user.enums.AuthProvider;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String mobileNumber;

    private String email;

    private String address;

    private String city;

    private String state;

    private String password;

    private String profileImage;

    private String role;

    private Boolean isEnable;

    private Boolean accountNonLocked;

    private Integer failedAttempt;

    private Date lockTime;

    private String resetToken;

    private Date resetTokenExpiry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    private String providerId;

    private Boolean emailVerified = false;

}
