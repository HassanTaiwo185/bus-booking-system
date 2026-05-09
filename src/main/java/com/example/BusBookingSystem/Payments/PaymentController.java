package com.example.BusBookingSystem.Payments;

import com.example.BusBookingSystem.Bookings.Bookings;
import com.example.BusBookingSystem.Bookings.BoookingsRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final StripeService stripeService;
    private final PaymentService paymentService;
    private final BoookingsRepository boookingsRepository;

    @GetMapping("/create-session/{bookingId}")
    public String createSession(@PathVariable Long bookingId) throws StripeException {

        Bookings booking = boookingsRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        Session session = stripeService.createCheckoutSession(booking);

        return "redirect:" + session.getUrl();
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("bookingId") Long bookingId, Model model) {

        paymentService.processSuccessfulPayment(bookingId);

        Bookings booking = boookingsRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        model.addAttribute("booking", booking);

        BigDecimal totalAmount = booking.getBusSchedule().getPrice()
                .multiply(BigDecimal.valueOf(booking.getNumberOfSeats()));

        model.addAttribute("total", totalAmount);

        return "payment-result";
    }
}