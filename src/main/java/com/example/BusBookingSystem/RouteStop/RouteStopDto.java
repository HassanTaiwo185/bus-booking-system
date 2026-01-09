package com.example.BusBookingSystem.RouteStop;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteStopDto {


    @NotNull
    private Long routeId;

    @NotNull
    private List<StopOrderRequest> stops = new java.util.ArrayList<>();;

    @Data
    public static class StopOrderRequest {
        @NotNull
        private Long stopId;

        @Positive
        private int stopOrder;
    }
}
