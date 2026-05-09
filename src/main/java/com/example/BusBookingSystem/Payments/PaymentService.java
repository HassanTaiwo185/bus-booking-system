package com.example.BusBookingSystem.Payments;

import com.example.BusBookingSystem.Bookings.BookingStatus;
import com.example.BusBookingSystem.Bookings.Bookings;
import com.example.BusBookingSystem.Bookings.BoookingsRepository;
import com.example.BusBookingSystem.Bus.BusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentsRepository paymentsRepository;
    private final BoookingsRepository boookingsRepository;
    private final BusRepository busRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void processSuccessfulPayment(Long bookingId) {

        Bookings booking = boookingsRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        boolean paymentExists =
                paymentsRepository.existsByBookingsId(bookingId);

        if (paymentExists) {
            return;
        }

        Payments payment = new Payments();

        payment.setBookings(booking);

        BigDecimal totalAmount =
                booking.getBusSchedule()
                        .getPrice()
                        .multiply(BigDecimal.valueOf(
                                booking.getNumberOfSeats()
                        ));

        payment.setAmount(totalAmount);
        payment.setPaidAt(Instant.now());

        paymentsRepository.save(payment);

        booking.setBookingStatus(BookingStatus.CONFIRMED);

        boookingsRepository.save(booking);

        var bus = booking.getBusSchedule().getBus();

        bus.setCapacity(
                bus.getCapacity() - booking.getNumberOfSeats()
        );

        busRepository.save(bus);

        sendBrevoEmail(booking);
    }

    private void sendBrevoEmail(Bookings booking) {

        String apiKey = System.getenv("BREVO_API_KEY");

        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new HashMap<>();

        body.put(
                "sender",
                Map.of(
                        "email", "ayindehassan776@gmail.com",
                        "name", "BusBooking"
                )
        );

        body.put(
                "to",
                List.of(
                        Map.of(
                                "email",
                                booking.getPassengerEmail()
                        )
                )
        );

        body.put(
                "subject",
                "Transtity - Booking Confirmed! Ticket #"
                        + booking.getId()
        );

        body.put(
                "textContent",
                "Hello " + booking.getPassengerName() + ",\n\n" +
                        "Your payment was successful! " +
                        "Your seat is reserved.\n\n" +

                        "Bus: " +
                        booking.getBusSchedule()
                                .getBus()
                                .getBusNumber() + "\n" +

                        "Seats: " +
                        booking.getNumberOfSeats() + "\n\n" +

                        "Safe travels!"
        );

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
    }
}