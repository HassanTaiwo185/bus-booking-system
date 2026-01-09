package com.example.BusBookingSystem.Payments;

import com.example.BusBookingSystem.Bookings.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    boolean existsByBookingsId(Long bookingId);

}
