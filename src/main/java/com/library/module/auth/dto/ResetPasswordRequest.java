package com.library.module.auth.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String email;
    private String password;
    private String resetToken;
}