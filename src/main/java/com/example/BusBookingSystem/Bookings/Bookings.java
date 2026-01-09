package com.example.BusBookingSystem.Bookings;


import com.example.BusBookingSystem.BusSchedules.BusSchedules;
import com.example.BusBookingSystem.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Bookings {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "busSchedule_id")
    private BusSchedules busSchedule;

    @Column(nullable = false)
    private Long numberOfSeats;

    @Column(nullable = false, length = 100)
    private String passengerName;

    @Column(nullable = false, length = 100)
    private String passengerEmail;

    @Enumerated(EnumType.STRING)
    private  BookingStatus bookingStatus;

    @Column(nullable = false)
    private Instant bookingDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }


}
