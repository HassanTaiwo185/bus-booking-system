package com.example.BusBookingSystem.BusSchedules;

import com.example.BusBookingSystem.Bus.BusService;
import com.example.BusBookingSystem.RouteStop.RouteStopService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/bus-schedules")
@RequiredArgsConstructor
public class BusSchedulesController {

    private final BusSchedulesService scheduleService;
    private final BusService busService;
    private final RouteStopService routeStopService;


    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/bus/list")
    public String listSchedules(Model model) {
        model.addAttribute("schedules", scheduleService.getAllSchedules());
        return "bus-schedules-list";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bus/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("schedule")) {
            model.addAttribute("schedule", new BusSchedulesRequestDto());
        }
        model.addAttribute("buses", busService.getAllBuses());
        model.addAttribute("routeStops", routeStopService.getActiveRouteStops());
        return "bus-schedules-create";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bus/save")
    public String saveOrUpdate(
            @Valid @ModelAttribute("schedule") BusSchedulesRequestDto dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(f ->
                    System.out.println("Field: " + f.getField() + " - Error: " + f.getDefaultMessage()));

            // Return the view directly to preserve errors and input
            model.addAttribute("buses", busService.getAllBuses());
            model.addAttribute("routeStops", routeStopService.getActiveRouteStops());
            return "bus-schedules-create";
        }

        try {
            scheduleService.saveOrUpdateSchedule(dto);
            return "redirect:/bus-schedules/bus/list";

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("buses", busService.getAllBuses());
            model.addAttribute("routeStops", routeStopService.getActiveRouteStops());
            return "bus-schedules-create";

        } catch (Exception e) {


            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("buses", busService.getAllBuses());
            model.addAttribute("routeStops", routeStopService.getActiveRouteStops());
            return "bus-schedules-create";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bus/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            BusSchedulesResponseDto response = scheduleService.getScheduleById(id);

            BusSchedulesRequestDto request = new BusSchedulesRequestDto();
            request.setId(response.getId());
            request.setBusId(response.getBus().getId());
            request.setRouteStopId(response.getRouteStop().getId());
            request.setScheduleDate(response.getScheduleDate());
            request.setDepartureTime(response.getDepartureTime());
            request.setPrice(response.getPrice());

            if (!model.containsAttribute("schedule")) {
                model.addAttribute("schedule", request);
            }
            model.addAttribute("buses", busService.getAllBuses());
            model.addAttribute("routeStops", routeStopService.getActiveRouteStops());
            return "bus-schedules-create";

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/bus-schedules/bus/list";
        }
    }
}