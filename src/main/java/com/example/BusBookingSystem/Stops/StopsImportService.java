package com.example.BusBookingSystem.Stops;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StopsImportService {

    private final StopsRepository stopsRepository;




    public void importStops(){


        // Read from stops.txt using buffer
        try(BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/stops.txt")))){

            String line;
            boolean skipFirstLine = true;

            // Read line to line if not empty
            while ((line = br.readLine()) != null) {
                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue;
                }

                // split  with comma
                String[] columns = line.split(",");


                if (columns.length < 6) {
                    continue;
                }

                String stopName = columns[2].replace("\"", "").trim();
                String latStr = columns[4].replace("\"", "").trim();
                String lonStr = columns[5].replace("\"", "").trim();

                if (stopName.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
                    continue;
                }

                // Check if stops name does not exist
                if (stopsRepository.findByStopName(stopName).isPresent()) {
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
        }catch (Exception e){
            throw new RuntimeException("Failed to import GTFS stops", e);

        }
    }
}
