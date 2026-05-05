package com.caronatracker.usersgroupsservice.service;

import com.caronatracker.usersgroupsservice.dto.VehicleRequest;
import com.caronatracker.usersgroupsservice.dto.VehicleResponse;
import com.caronatracker.usersgroupsservice.entity.FuelType;
import com.caronatracker.usersgroupsservice.entity.User;
import com.caronatracker.usersgroupsservice.entity.Vehicle;
import com.caronatracker.usersgroupsservice.exception.AccessDeniedException;
import com.caronatracker.usersgroupsservice.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void createVehicle_success() {
        UUID ownerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        when(userService.findById(ownerId)).thenReturn(owner);

        VehicleRequest request = buildVehicleRequest(BigDecimal.valueOf(12.5));
        Vehicle saved = buildVehicle(UUID.randomUUID(), owner, true);
        saved.setAvgKmPerLiter(BigDecimal.valueOf(12.5));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(saved);

        VehicleResponse response = vehicleService.createVehicle(ownerId, request);

        assertNotNull(response);
        assertEquals(ownerId, response.ownerId());
    }

    @Test
    void updateVehicle_byNonOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID nonOwnerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Vehicle vehicle = buildVehicle(UUID.randomUUID(), owner, true);
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        VehicleRequest request = buildVehicleRequest(BigDecimal.valueOf(10.0));

        assertThrows(AccessDeniedException.class,
                () -> vehicleService.updateVehicle(vehicle.getId(), nonOwnerId, request));
    }

    @Test
    void updateAvgKmPerLiter_success() {
        UUID ownerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Vehicle vehicle = buildVehicle(UUID.randomUUID(), owner, true);
        vehicle.setAvgKmPerLiter(BigDecimal.valueOf(10.0));
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            return v;
        });

        VehicleRequest request = new VehicleRequest("Carro", "ABC-1234", "Toyota", "Corolla",
                2022, BigDecimal.valueOf(15.0), FuelType.FLEX);
        VehicleResponse response = vehicleService.updateVehicle(vehicle.getId(), ownerId, request);

        assertEquals(BigDecimal.valueOf(15.0), response.avgKmPerLiter());
    }

    @Test
    void setVehicleStatus_inactive() {
        UUID ownerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Vehicle vehicle = buildVehicle(UUID.randomUUID(), owner, true);
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleResponse response = vehicleService.setVehicleStatus(vehicle.getId(), ownerId, false);

        assertFalse(response.active());
    }

    @Test
    void setGroupActiveVehicle_withInactiveVehicle() {
        UUID vehicleId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID());
        Vehicle vehicle = buildVehicle(vehicleId, owner, false);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        assertThrows(IllegalArgumentException.class,
                () -> vehicleService.getActiveVehicleForGroup(vehicleId));
    }

    private User buildUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("Usuário Teste");
        user.setEmail("test@email.com");
        user.setHasCnh(false);
        return user;
    }

    private Vehicle buildVehicle(UUID id, User owner, boolean active) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setOwner(owner);
        vehicle.setNickname("Carro");
        vehicle.setPlate("ABC-1234");
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setYear(2022);
        vehicle.setAvgKmPerLiter(BigDecimal.valueOf(12.0));
        vehicle.setFuelType(FuelType.FLEX);
        vehicle.setActive(active);
        return vehicle;
    }

    private VehicleRequest buildVehicleRequest(BigDecimal avgKm) {
        return new VehicleRequest("Carro", "ABC-1234", "Toyota", "Corolla", 2022, avgKm, FuelType.FLEX);
    }
}
