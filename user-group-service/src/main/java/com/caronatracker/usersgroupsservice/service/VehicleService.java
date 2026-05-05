package com.caronatracker.usersgroupsservice.service;

import com.caronatracker.usersgroupsservice.dto.VehicleRequest;
import com.caronatracker.usersgroupsservice.dto.VehicleResponse;
import com.caronatracker.usersgroupsservice.entity.User;
import com.caronatracker.usersgroupsservice.entity.Vehicle;
import com.caronatracker.usersgroupsservice.exception.AccessDeniedException;
import com.caronatracker.usersgroupsservice.exception.EntityNotFoundException;
import com.caronatracker.usersgroupsservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserService userService;

    public VehicleResponse createVehicle(UUID userId, VehicleRequest request) {
        User owner = userService.findById(userId);
        Vehicle vehicle = new Vehicle();
        vehicle.setOwner(owner);
        vehicle.setNickname(request.nickname());
        vehicle.setPlate(request.plate());
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setAvgKmPerLiter(request.avgKmPerLiter());
        vehicle.setFuelType(request.fuelType());
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> listVehicles(UUID userId) {
        return vehicleRepository.findByOwnerId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public VehicleResponse updateVehicle(UUID vehicleId, UUID currentUserId, VehicleRequest request) {
        Vehicle vehicle = findById(vehicleId);
        checkOwner(vehicle, currentUserId);
        vehicle.setNickname(request.nickname());
        vehicle.setPlate(request.plate());
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setAvgKmPerLiter(request.avgKmPerLiter());
        vehicle.setFuelType(request.fuelType());
        return toResponse(vehicleRepository.save(vehicle));
    }

    public VehicleResponse setVehicleStatus(UUID vehicleId, UUID currentUserId, boolean active) {
        Vehicle vehicle = findById(vehicleId);
        checkOwner(vehicle, currentUserId);
        vehicle.setActive(active);
        return toResponse(vehicleRepository.save(vehicle));
    }

    public Vehicle getActiveVehicleForGroup(UUID vehicleId) {
        Vehicle vehicle = findById(vehicleId);
        if (!Boolean.TRUE.equals(vehicle.getActive())) {
            throw new IllegalArgumentException("Veículo inativo não pode ser definido como veículo ativo do grupo");
        }
        return vehicle;
    }

    @Transactional(readOnly = true)
    public Vehicle findById(UUID vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado: " + vehicleId));
    }

    private void checkOwner(Vehicle vehicle, UUID currentUserId) {
        if (!vehicle.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Apenas o dono do veículo pode realizar esta operação");
        }
    }

    VehicleResponse toResponse(Vehicle v) {
        return new VehicleResponse(
                v.getId(),
                v.getOwner().getId(),
                v.getNickname(),
                v.getPlate(),
                v.getBrand(),
                v.getModel(),
                v.getYear(),
                v.getAvgKmPerLiter(),
                v.getFuelType(),
                v.getActive(),
                v.getCreatedAt()
        );
    }
}
