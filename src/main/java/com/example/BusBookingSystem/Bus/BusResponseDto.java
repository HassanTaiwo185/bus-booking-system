package com.example.BusBookingSystem.Bus;

import com.example.BusBookingSystem.User.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusResponseDto {


    private Long id;
    private String busNumber;
    private Integer capacity;
    private BusType type;
    private BusStatus status;
    private Instant createdAt;
    private User createdBy;
    private Instant updatedAt;
}
