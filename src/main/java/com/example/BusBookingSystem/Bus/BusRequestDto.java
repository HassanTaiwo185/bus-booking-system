package com.example.BusBookingSystem.Bus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusRequestDto {

    @NotBlank(message = "Bus number is required")
    private String busNumber;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Long capacity;

    @NotNull(message = "Bus type is required")
    private BusType type;

    @NotNull(message = "Status is required")
    private BusStatus status = BusStatus.ACTIVE;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;
}