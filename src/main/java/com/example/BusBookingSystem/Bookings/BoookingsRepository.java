package com.example.BusBookingSystem.Bookings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoookingsRepository extends JpaRepository<Bookings, Long> {
    List<Bookings> findByUserEmail(String email);

}
