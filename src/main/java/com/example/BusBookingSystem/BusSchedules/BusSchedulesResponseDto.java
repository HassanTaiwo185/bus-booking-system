package com.example.BusBookingSystem.BusSchedules;

import com.example.BusBookingSystem.Bus.Bus;
import com.example.BusBookingSystem.RouteStop.RouteStop;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusSchedulesResponseDto {


    private Long id;
    private Bus bus;
    private RouteStop routeStop;
    private LocalDate scheduleDate;
    private LocalTime departureTime;
    private BigDecimal price;
    private ScheduleStatus status;
}
