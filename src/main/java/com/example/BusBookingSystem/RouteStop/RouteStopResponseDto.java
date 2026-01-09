package com.example.BusBookingSystem.RouteStop;


import jakarta.persistence.Column;
import lombok.Data;

import java.time.Instant;

@Data
public class RouteStopResponseDto {

    private Long id;
    private Long stopId;
    private String stopName;
    private int stopOrder;
    private String routeName;
    private Instant validFrom;
    private Instant validTo;

}
