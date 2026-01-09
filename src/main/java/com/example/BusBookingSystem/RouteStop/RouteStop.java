package com.example.BusBookingSystem.RouteStop;

import com.example.BusBookingSystem.Routes.Routes;
import com.example.BusBookingSystem.Stops.Stops;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Entity
@Table(name = "route_stops")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RouteStop {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Routes route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stops stop;

    @Column(nullable = false)
    private int stopOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant validFrom;

    @Column
    private Instant validTo;

    @PrePersist
    void onCreate() {
        this.validFrom = Instant.now();
        this.active = true;
    }
}
