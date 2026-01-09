package com.example.BusBookingSystem.Bus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
