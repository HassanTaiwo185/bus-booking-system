package com.example.BusBookingSystem.Payments;

import com.example.BusBookingSystem.Bookings.BookingStatus;
import com.example.BusBookingSystem.Bookings.Bookings;
import com.example.BusBookingSystem.Bookings.BoookingsRepository;
import com.example.BusBookingSystem.Bus.BusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentsRepository paymentsRepository;
    private final BoookingsRepository boookingsRepository;
    private final BusRepository busRepository;
    private final JavaMailSender mailSender;


    @Transactional
    public void processSuccessfulPayment(Long bookingId) {
        Bookings booking = boookingsRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        boolean paymentExists = paymentsRepository.existsByBookingsId(bookingId);

        if (paymentExists) {
            return;
        }


        Payments payment = new Payments();
        payment.setBookings(booking);
        BigDecimal totalAmount = booking.getBusSchedule().getPrice()
                .multiply(BigDecimal.valueOf(booking.getNumberOfSeats()));
        payment.setAmount(totalAmount);
        payment.setPaidAt(Instant.now());
        paymentsRepository.save(payment);


        booking.setBookingStatus(BookingStatus.CONFIRMED);
        boookingsRepository.save(booking);


        var bus = booking.getBusSchedule().getBus();

        bus.setCapacity(bus.getCapacity() - booking.getNumberOfSeats());
        busRepository.save(bus);

        sendConfirmationEmail(booking);
    }



    private void sendConfirmationEmail(Bookings booking) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(booking.getPassengerEmail());
        message.setSubject("Transtity - Booking Confirmed! Ticket #" + booking.getId());
        message.setText("Hello " + booking.getPassengerName() + ",\n\n" +
                "Your payment was successful! Your seat is reserved.\n" +
                "Bus: " + booking.getBusSchedule().getBus().getBusNumber() + "\n" +
                "Seats: " + booking.getNumberOfSeats() + "\n\n" +
                "Safe travels!");

        mailSender.send(message);
    }
}
