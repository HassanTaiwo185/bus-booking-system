package com.example.BusBookingSystem.Stops;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;


@Table(name = "stops")
@Entity
@NoArgsConstructor
@Data
public class Stops {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 100,unique = true)
    private String stopName;

    @Column(nullable = false, length = 100)
    private BigDecimal latitude;

    @Column(nullable = false, length = 100)
    private BigDecimal longitude;

    @Column(nullable = false, length = 100)
    private Instant createdDate;



}
