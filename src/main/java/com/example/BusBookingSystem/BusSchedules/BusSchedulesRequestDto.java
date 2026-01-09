package com.example.BusBookingSystem.BusSchedules;

import com.example.BusBookingSystem.Bus.Bus;
import com.example.BusBookingSystem.RouteStop.RouteStop;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusSchedulesRequestDto {

    private Long id;

    @NotNull(message = "Please select a bus")
    private Long busId;

    @NotNull(message = "Route stop selection is required")
    private Long routeStopId;

    @NotNull(message = "Schedule date required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    @NotNull(message = "Departure time required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime departureTime;

    @NotNull(message = "Price is required")
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    private BigDecimal price;


}
