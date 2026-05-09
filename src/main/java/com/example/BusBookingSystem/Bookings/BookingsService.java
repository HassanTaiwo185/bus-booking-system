package com.example.BusBookingSystem.Bookings;

import com.example.BusBookingSystem.Bus.Bus;
import com.example.BusBookingSystem.Bus.BusRepository;
import com.example.BusBookingSystem.BusSchedules.BusSchedules;
import com.example.BusBookingSystem.BusSchedules.BusSchedulesRepository;
import com.example.BusBookingSystem.User.User;
import com.example.BusBookingSystem.User.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingsService {

    private final BusSchedulesRepository busSchedulesRepository;
    private final BoookingsRepository boookingsRepository;
    private final UserRepository userRepository;
    private final BusRepository busRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Bookings> findAll() {

        // DO NOT throw exception here
        return boookingsRepository.findAll();
    }

    public List<Bookings> getMyBookingHistory() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        // Return empty list normally
        return boookingsRepository.findByUserEmail(email);
    }

    @Transactional
    public Long createBooking(BookingRequestDto dto) {

        BusSchedules busSchedules =
                busSchedulesRepository.findById(dto.getBusScheduleId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Invalid Bus Schedule ID"));

        Bus bus = busSchedules.getBus();

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (bus.getCapacity() < dto.getNumberOfSeats()) {
            throw new RuntimeException(
                    "Not enough seats! Only " +
                            bus.getCapacity() +
                            " left."
            );
        }

        Bookings booking = Bookings.builder()
                .user(user)
                .busSchedule(busSchedules)
                .passengerName(
                        user.getFirstName() +
                                " " +
                                user.getLastName()
                )
                .passengerEmail(user.getEmail())
                .numberOfSeats(dto.getNumberOfSeats())
                .bookingStatus(BookingStatus.PENDING)
                .bookingDate(Instant.now())
                .build();

        Bookings saved = boookingsRepository.save(booking);

        return saved.getId();
    }

    @Transactional
    public void cancelBooking(CancelBookingsRequestDto dto) {

        Bookings booking = boookingsRepository.findById(dto.getBookingId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        Bus bus = booking.getBusSchedule().getBus();

        User user = (User)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException(
                    "You are not authorized to cancel this booking"
            );
        }

        LocalTime depTime =
                booking.getBusSchedule().getDepartureTime();

        LocalDateTime departureDateTime =
                LocalDateTime.of(LocalDate.now(), depTime);

        if (departureDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "You cannot cancel this booking. The bus has already departed."
            );
        }

        if (booking.getBookingStatus().equals(BookingStatus.CANCELLED)) {
            throw new IllegalStateException(
                    "Booking already cancelled."
            );
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        booking = boookingsRepository.save(booking);

        bus.setCapacity(
                bus.getCapacity() + booking.getNumberOfSeats()
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
                        "name", "Transtity"
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
                "Transtity - Booking Cancelled! Ticket #"
                        + booking.getId()
        );

        body.put(
                "textContent",
                "Hello " + booking.getPassengerName() + ",\n\n" +

                        "Your booking has been cancelled.\n" +

                        "Refund will be processed in 5 business days.\n\n" +

                        "Bus: " +
                        booking.getBusSchedule()
                                .getBus()
                                .getBusNumber() + "\n" +

                        "Seats: " +
                        booking.getNumberOfSeats() + "\n\n" +

                        "Thank you for using Transtity."
        );

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
    }
}