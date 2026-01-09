package com.example.BusBookingSystem.BusSchedules;


import com.example.BusBookingSystem.Bus.Bus;
import com.example.BusBookingSystem.Bus.BusRepository;
import com.example.BusBookingSystem.Bus.BusStatus;
import com.example.BusBookingSystem.RouteStop.RouteStop;
import com.example.BusBookingSystem.RouteStop.RouteStopRepository;
import com.example.BusBookingSystem.Routes.Status;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.RouteMatcher;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusSchedulesService {


    private final BusSchedulesRepository scheduleRepository;
    private final BusRepository busRepository;
    private final RouteStopRepository routeStopRepository;
    private final BusSchedulesMapper mapper;

    @Transactional
    public void saveOrUpdateSchedule(BusSchedulesRequestDto dto) {

        if (dto.getBusId() == null) {
            throw new IllegalArgumentException("Validation Failed: You must select a Bus.");
        }
        if (dto.getRouteStopId() == null) {
            throw new IllegalArgumentException("Validation Failed: You must select a Route Stop.");
        }

        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Validation Failed: Price must be greater than zero");
        }

        Bus bus = busRepository.findById(dto.getBusId())
                .orElseThrow(() -> new EntityNotFoundException("The selected Bus no longer exists."));

        if (!bus.getStatus().equals(BusStatus.ACTIVE)) {
            throw new IllegalArgumentException("Validation Failed: Cannot schedule bus " + bus.getBusNumber() +
                    " because its current status is " + bus.getStatus());
        }


        RouteStop routeStop = routeStopRepository.findById(dto.getRouteStopId())
                .orElseThrow(() -> new EntityNotFoundException("Route Stop not found"));



        if (!routeStop.getRoute().getStatus().equals(Status.ACTIVE)) {
            throw new IllegalArgumentException("Validation Failed: The route '" +
                    routeStop.getRoute().getRouteName() + "' is currently inactive and cannot be scheduled.");
        }

        if (dto.getId() != null && dto.getId() > 0) {
            BusSchedules existingSchedule = scheduleRepository.findById(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Schedule not found."));


            mapper.updateEntityFromDto(dto, existingSchedule);


            existingSchedule.setBus(busRepository.getReferenceById(dto.getBusId()));
            existingSchedule.setRouteStop(routeStopRepository.getReferenceById(dto.getRouteStopId()));
        } else {
            BusSchedules newSchedule = mapper.toEntity(dto);
            scheduleRepository.save(newSchedule);
        }
    }

    public List<BusSchedulesResponseDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public BusSchedulesResponseDto getScheduleById(Long id) {
        BusSchedules schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with ID: " + id));
        return mapper.toResponse(schedule);
    }


}
