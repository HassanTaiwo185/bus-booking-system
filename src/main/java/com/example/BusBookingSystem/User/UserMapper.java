package com.example.BusBookingSystem.User;


import com.example.BusBookingSystem.Auth.LoginRequestDTO;
import com.example.BusBookingSystem.Auth.LoginResponseDto;
import com.example.BusBookingSystem.Auth.RegisterRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User registerDtoToEntity(RegisterRequestDTO dto);

    LoginResponseDto userToLoginResponseDto(User user);

}
