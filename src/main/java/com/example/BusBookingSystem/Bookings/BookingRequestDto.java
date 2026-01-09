package com.example.BusBookingSystem.Bookings;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
public class BookingRequestDto {

    @NotNull(message = "Please select a bus Schedule to book")
    private Long busScheduleId;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "You must book at least 1 seat")
    private Long numberOfSeats;


}
