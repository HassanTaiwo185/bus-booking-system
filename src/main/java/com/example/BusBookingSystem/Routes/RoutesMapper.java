package com.example.BusBookingSystem.Routes;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoutesMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "routeStops", ignore = true)
    Routes toEntity(RoutesRequestDto dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "routeStops", ignore = true)
    void updateEntityFromDto(RoutesRequestDto dto, @MappingTarget Routes route);

    RoutesResponseDto toResponseDto(Routes route);

    RoutesRequestDto toRequestDto(Routes route);
}