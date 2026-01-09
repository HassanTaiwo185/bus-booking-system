package com.example.BusBookingSystem.Payments;


import com.example.BusBookingSystem.Bookings.Bookings;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Bookings bookings;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant paidAt;




}
