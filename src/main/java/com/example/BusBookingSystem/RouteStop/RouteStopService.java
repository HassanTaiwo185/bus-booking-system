package com.example.BusBookingSystem.RouteStop;

import com.example.BusBookingSystem.Routes.Routes;
import com.example.BusBookingSystem.Routes.RoutesRepository;
import com.example.BusBookingSystem.Routes.Status;
import com.example.BusBookingSystem.Stops.Stops;
import com.example.BusBookingSystem.Stops.StopsRepository;
import com.example.BusBookingSystem.User.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteStopService {

    private final RouteStopRepository routeStopRepository;
    private final RoutesRepository routesRepository;
    private final StopsRepository stopsRepository;
    private final RouteStopMapper routeStopMapper;

    @Transactional
    public void saveRouteStops(RouteStopDto request) {
        Routes route = routesRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));

        Instant now = Instant.now();

        if (request.getStops() == null || request.getStops().isEmpty()) {
            throw new RuntimeException("Cannot save a route without any stops. Please select at least one stop.");
        }

        if (route.getStatus() == Status.INACTIVE) {
            throw new RuntimeException("Cannot update stops for an Inactive route.");
        }

        //  Archive the old (The "History")
        List<RouteStop> oldStops = routeStopRepository.findByRouteAndActiveTrue(route);
        oldStops.forEach(rs -> {
            rs.setActive(false);
            rs.setValidTo(now);
        });
        routeStopRepository.saveAllAndFlush(oldStops);

        int order = 1;

            for (RouteStopDto.StopOrderRequest s : request.getStops()) {


                if (s == null || s.getStopId() == null) {
                    continue;
                }

                RouteStop newStop = RouteStop.builder()
                        .route(route)
                        .stop(stopsRepository.getReferenceById(s.getStopId()))
                        .stopOrder(order++)
                        .active(true)
                        .validFrom(now)
                        .build();
                routeStopRepository.save(newStop);
            }

    }


    public List<RouteStopResponseDto> getActiveRouteStops(Long routeId) {

        Routes route = routesRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        return routeStopRepository
                .findByRouteAndActiveTrueOrderByStopOrderAsc(route)
                .stream()
                .map(routeStopMapper::toDto)
                .toList();
    }

    public List<RouteStopResponseDto>  getActiveRouteStops() {
        return routeStopRepository.findAllActive()
                .stream()
                .map(routeStopMapper::toDto)
                .toList();
    }
}
