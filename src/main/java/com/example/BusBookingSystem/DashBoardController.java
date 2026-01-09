package com.example.BusBookingSystem;


import com.example.BusBookingSystem.BusSchedules.BusSchedulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashBoardController {

    private final BusSchedulesService busSchedulesService;

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "admin-dashboard";
    }


    @GetMapping("/user-dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(Model model) {
        model.addAttribute("schedules", busSchedulesService.getAllSchedules());
        return "user-dashboard";
    }
}
