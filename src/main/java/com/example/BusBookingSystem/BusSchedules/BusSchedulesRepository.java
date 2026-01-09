package com.example.BusBookingSystem.BusSchedules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusSchedulesRepository extends JpaRepository<BusSchedules, Long> {
}
