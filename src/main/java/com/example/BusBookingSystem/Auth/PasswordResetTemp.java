package com.example.BusBookingSystem.Auth;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PasswordResetTemp {

    private String email;
    private String hashedPassword;
}
