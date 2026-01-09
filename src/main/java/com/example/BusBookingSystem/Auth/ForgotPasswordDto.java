package com.example.BusBookingSystem.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDto {

    @NotBlank(message = "Email can not be blank")
    private String email;

    @NotBlank(message = "Password can not be blank")
    private String password;

    @NotBlank(message = "Confirm password can not be black")
    private String confirmPassword;
}
