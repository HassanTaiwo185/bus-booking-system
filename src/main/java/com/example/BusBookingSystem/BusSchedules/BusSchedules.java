package com.example.BusBookingSystem.BusSchedules;

import com.example.BusBookingSystem.Bus.Bus;
import com.example.BusBookingSystem.RouteStop.RouteStop;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;



@Entity
@Table(name = "bus_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusSchedules {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_stop_id")
    private RouteStop routeStop;

    @Column(nullable = false)
    private LocalDate scheduleDate;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    @Positive(message = "Price must be greater than zero")
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;


    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;



}
