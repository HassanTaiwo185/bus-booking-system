package com.example.BusBookingSystem.Stops;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

            InputStream inputStream =
                    getClass().getResourceAsStream("/stops.txt");

            if (inputStream == null) {
                System.out.println("stops.txt not found");
                return;
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            );

            String line;
            boolean skipHeader = true;

            List<Stops> stopsList = new ArrayList<>();

            while ((line = br.readLine()) != null) {

                // Skip header
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                // Proper CSV split (handles commas inside quotes)
                String[] columns =
                        line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (columns.length < 6) {
                    continue;
                }

                String stopName =
                        columns[2].replace("\"", "").trim();

                String latStr =
                        columns[4].replace("\"", "").trim();

                String lonStr =
                        columns[5].replace("\"", "").trim();

                if (stopName.isBlank()
                        || latStr.isBlank()
                        || lonStr.isBlank()) {
                    continue;
                }

                Stops stop = new Stops();

                stop.setStopName(stopName);
                stop.setLatitude(new BigDecimal(latStr));
                stop.setLongitude(new BigDecimal(lonStr));
                stop.setCreatedDate(Instant.now());

                stopsList.add(stop);
            }

            stopsRepository.saveAll(stopsList);

            System.out.println(
                    "Imported " + stopsList.size() + " stops successfully"
            );

        } catch (Exception e) {

            // Don't crash deployment
            e.printStackTrace();

            System.out.println(
                    "GTFS import failed but application will continue running"
            );
        }
    }
}