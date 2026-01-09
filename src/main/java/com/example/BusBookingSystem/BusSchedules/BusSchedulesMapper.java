package com.example.BusBookingSystem.BusSchedules;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BusSchedulesMapper {

    BusSchedulesResponseDto toResponse(BusSchedules entity);

    @Mapping(target = "bus.id", source = "busId")
    @Mapping(target = "routeStop.id", source = "routeStopId")
    BusSchedules toEntity(BusSchedulesRequestDto dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "routeStop", ignore = true)
    void updateEntityFromDto(BusSchedulesRequestDto dto, @MappingTarget BusSchedules entity);
}