package com.example.BusBookingSystem.Stops;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StopsRunner implements CommandLineRunner {

    private final StopsImportService stopsImportService;

    @Override
    public void run(String... args) {
        stopsImportService.importStops();
    }
}
