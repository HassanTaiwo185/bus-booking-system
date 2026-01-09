package com.example.BusBookingSystem.Stops;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StopsRepository extends JpaRepository<Stops, Long> {

    Optional<Stops> findByStopName(String stopName);
}
