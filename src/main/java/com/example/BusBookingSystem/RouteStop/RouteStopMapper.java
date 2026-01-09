package com.example.BusBookingSystem.RouteStop;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RouteStopMapper {

    @Mapping(source = "stop.id", target = "stopId")
    @Mapping(source = "stop.stopName", target = "stopName")
    @Mapping(target = "routeName", source = "route.routeName")
    RouteStopResponseDto toDto(RouteStop entity);
}
