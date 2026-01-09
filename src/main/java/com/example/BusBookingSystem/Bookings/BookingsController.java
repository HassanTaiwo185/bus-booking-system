package com.example.BusBookingSystem.Bookings;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingsController {

    private final BookingsService bookingsService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingsService.findAll());
        return "bookings-list";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-history")
    public String myHistory(Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("bookings", bookingsService.getMyBookingHistory());
            return "bookings-my-list";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/user-dashboard";
        }
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping("/new")
    public String createBookingPage(@RequestParam(required = false) Long scheduleId, Model model) {
        if (!model.containsAttribute("bookingRequest")) {
            BookingRequestDto dto = new BookingRequestDto();
            dto.setBusScheduleId(scheduleId);
            model.addAttribute("bookingRequest", dto);
        }
        return "bookings-create";
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create")
    public String createBooking(@Valid @ModelAttribute("bookingRequest") BookingRequestDto dto,
                                BindingResult result,
                                RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookingRequest", result);
            ra.addFlashAttribute("bookingRequest", dto);
            return "redirect:/bookings/new?scheduleId=" + dto.getBusScheduleId();
        }

        try {
            Long bookingId = bookingsService.createBooking(dto);
            ra.addFlashAttribute("Pending", "Booking initiated! Please check your email.");

            return "redirect:/payment/create-session/" + bookingId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("bookingRequest", dto);
            return "redirect:/bookings/new?scheduleId=" + dto.getBusScheduleId();
        }
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/cancel")
    public String cancelBooking(@ModelAttribute CancelBookingsRequestDto cancelRequest,
                                RedirectAttributes ra) {
        try {
            bookingsService.cancelBooking(cancelRequest);
            ra.addFlashAttribute("success", "Booking cancelled successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings/my-history";
    }
}