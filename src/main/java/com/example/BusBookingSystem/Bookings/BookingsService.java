package com.example.BusBookingSystem.Bookings;


import com.example.BusBookingSystem.Bus.Bus;
import com.example.BusBookingSystem.Bus.BusRepository;
import com.example.BusBookingSystem.BusSchedules.BusSchedules;
import com.example.BusBookingSystem.BusSchedules.BusSchedulesRepository;
import com.example.BusBookingSystem.User.User;
import com.example.BusBookingSystem.User.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingsService {


    private final BusSchedulesRepository busSchedulesRepository;
    private  final BoookingsRepository boookingsRepository;
    private  final UserRepository userRepository;
    private final BusRepository busRepository;
    private final MailSender mailSender;



    public List<Bookings> findAll() {
        List<Bookings> bookings = boookingsRepository.findAll();
        if (bookings.isEmpty()) {
            throw new RuntimeException("No bookings found in the system.");
        }
        return bookings;
    }

    public List<Bookings> getMyBookingHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Bookings> history = boookingsRepository.findByUserEmail(email);

        if (history.isEmpty()) {

            throw new RuntimeException("You have no booking history yet.");
        }
        return history;
    }

    @Transactional
    public Long createBooking(BookingRequestDto dto) {

        BusSchedules busSchedules = busSchedulesRepository.findById(dto.getBusScheduleId()).orElseThrow(() -> new IllegalArgumentException("Invalid Bus Schedule ID"));

        Bus bus = busSchedules.getBus();

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (bus.getCapacity() < dto.getNumberOfSeats()) {
            throw new RuntimeException("Not enough seats! Only " + bus.getCapacity() + " left.");
        }

        Bookings booking = Bookings.builder()
                .user(user)
                .busSchedule(busSchedules)
                .passengerName(user.getFirstName() + " " + user.getLastName())
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
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        Bus bus = booking.getBusSchedule().getBus();

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You are not authorized to cancel this booking");
        }

        LocalTime depTime = booking.getBusSchedule().getDepartureTime();
        LocalDateTime departureDateTime = LocalDateTime.of(LocalDate.now(), depTime);


        if (departureDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("You cannot cancel this booking. The bus has already departed.");
        }

        if(booking.getBookingStatus().equals(BookingStatus.CANCELLED)){
            throw new IllegalStateException("You can not cancel this booking. Booking is already cancelled.");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking =  boookingsRepository.save(booking);

        bus.setCapacity(bus.getCapacity() + booking.getNumberOfSeats());
        busRepository.save(bus);
        sendConfirmationEmail(booking);



    }


    private void sendConfirmationEmail(Bookings booking) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(booking.getPassengerEmail());
        message.setSubject("Transtity - Booking Cancelled! Ticket #" + booking.getId());
        message.setText("Hello " + booking.getPassengerName() + ",\n\n" +
                "Booking Cancelled, Your payment will be refunded in 5 business days! Your seat is reserved.\n" +
                "Bus: " + booking.getBusSchedule().getBus().getBusNumber() + "\n" +
                "Seats: " + booking.getNumberOfSeats() + "\n\n" +
                "Safe travels!");

        mailSender.send(message);
    }
}
