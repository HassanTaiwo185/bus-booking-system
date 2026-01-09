package com.example.BusBookingSystem.Routes;

import com.example.BusBookingSystem.User.User;
import lombok.Data;

import java.time.Instant;

@Data
public class RoutesResponseDto {

    private Long id;
    private String routeName;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;
    private User createdBy;
}
