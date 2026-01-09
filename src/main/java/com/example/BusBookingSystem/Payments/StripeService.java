package com.example.BusBookingSystem.Payments;

import com.example.BusBookingSystem.Bookings.Bookings;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String secretKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }


    public Session createCheckoutSession(Bookings bookings) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&bookingId=" + bookings.getId())
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("cad")
                                .setUnitAmount(bookings.getBusSchedule().getPrice()
                                        .multiply(BigDecimal.valueOf(bookings.getNumberOfSeats()))
                                        .multiply(new BigDecimal(100))
                                        .longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Bus Ticket: " + bookings.getBusSchedule().getRouteStop().getRoute().getRouteName())
                                        .setDescription("Booking ID: #" + bookings.getId() + " | Seats: " + bookings.getNumberOfSeats())
                                        .build())
                                .build())
                        .build())
                .build();

        return Session.create(params);

    }
}
