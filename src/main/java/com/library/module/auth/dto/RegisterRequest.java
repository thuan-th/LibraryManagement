package com.library.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Tên không được để trống")
    @Pattern(regexp = "^[A-Za-zÀ-ỹ\\s]+$", message = "Tên chỉ được chứa chữ")
    private String name;

    @NotBlank(message = "SĐT không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "SĐT phải 10 số")
    private String mobileNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Xã/Phường không được để trống")
    private String state;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String city;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 5, message = "Mật khẩu tối thiểu 5 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String cpassword;
}