package com.example.BusBookingSystem.Stops;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StopsImportService {

    private final StopsRepository stopsRepository;

    @PostConstruct
    public void importStops() {

        try {

            // Prevent duplicate imports
            if (stopsRepository.count() > 0) {
                System.out.println("Stops already imported");
                return;
            }

            // Load file from resources
            InputStream inputStream =
                    getClass().getResourceAsStream("/stops.txt");

            if (inputStream == null) {
                throw new RuntimeException("stops.txt not found in resources");
            }

            BufferedReader br =
                    new BufferedReader(new InputStreamReader(inputStream));

            String line;
            boolean skipFirstLine = true;

            while ((line = br.readLine()) != null) {

                // Skip CSV header
                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue;
                }

                String[] columns = line.split(",");

                if (columns.length < 6) {
                    continue;
                }

                String stopName =
                        columns[2].replace("\"", "").trim();

                String latStr =
                        columns[4].replace("\"", "").trim();

                String lonStr =
                        columns[5].replace("\"", "").trim();

                if (stopName.isEmpty()
                        || latStr.isEmpty()
                        || lonStr.isEmpty()) {
                    continue;
                }

                BigDecimal latitude = new BigDecimal(latStr);
                BigDecimal longitude = new BigDecimal(lonStr);

                Stops stop = new Stops();

                stop.setStopName(stopName);
                stop.setLatitude(latitude);
                stop.setLongitude(longitude);
                stop.setCreatedDate(Instant.now());

                stopsRepository.save(stop);
            }

            System.out.println("Stops imported successfully");

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to import GTFS stops",
                    e
            );
        }
    }
}