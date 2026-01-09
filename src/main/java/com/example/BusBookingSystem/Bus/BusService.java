package com.example.BusBookingSystem.Bus;

import com.example.BusBookingSystem.User.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;
    private final BusMapper busMapper;

    public List<BusResponseDto> getAllBuses() {
        return busRepository.findAll().stream()
                .map(busMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void addBus(BusRequestDto dto, User user) {
        Bus bus = busMapper.toEntity(dto);
        if (busRepository.existsByBusNumber(dto.getBusNumber())) {
            throw new RuntimeException("Bus number " + dto.getBusNumber() + " already exists!");
        }

        if (dto.getCapacity() == null || dto.getCapacity() <= 0) {
            throw new IllegalArgumentException("Validation Failed: Bus capacity must be greater than zero");
        }

        bus.setCreatedBy(user);
        busRepository.save(bus);
    }

    @Transactional
    public BusResponseDto getBusForEdit(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus not found"));


        return busMapper.toResponseDto(bus);
    }
    @Transactional
    public void updateBus(Long id, BusRequestDto dto) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        busMapper.updateEntityFromDto(dto, bus);
        busRepository.save(bus);
    }
}
