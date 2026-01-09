package com.example.BusBookingSystem.Routes;

import com.example.BusBookingSystem.User.Role;
import com.example.BusBookingSystem.User.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutesService {

    private final RoutesRepository routesRepository;
    private final RoutesMapper routesMapper;

    public void addRoute(RoutesRequestDto request, User currentUser) {
        Routes route = routesMapper.toEntity(request);
        route.setStatus(Status.ACTIVE);
        route.setCreatedBy(currentUser);
        routesRepository.save(route);
    }

    public RoutesRequestDto getRouteForEdit(Long routeId) {
        Routes route = routesRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        return routesMapper.toRequestDto(route);
    }

    @Transactional
    public void updateRoute(Long routeId, RoutesRequestDto request, User currentUser) {
        Routes route = routesRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (!route.getCreatedBy().getId().equals(currentUser.getId())
                && currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized to update this route");
        }


        routesMapper.updateEntityFromDto(request, route);

    }

    public List<Routes> getAllRoutes() {
        return routesRepository.findAll();
    }
}