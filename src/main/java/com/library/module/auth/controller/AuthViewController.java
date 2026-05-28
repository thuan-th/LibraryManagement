package com.library.module.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AuthViewController {

    @GetMapping("/signin")
    public String login(Principal principal) {

        if (principal != null) {
            return "redirect:/";
        }

        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Principal principal) {

        if (principal != null) {
            return "redirect:/";
        }

        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Principal principal) {
        if (principal != null) {
            return "redirect:/";
        }

        return "forgot_password";
    }
}