package com.caronatracker.trips.controller;

import com.caronatracker.trips.dto.ParticipantResponse;
import com.caronatracker.trips.dto.TripRequest;
import com.caronatracker.trips.dto.TripResponse;
import com.caronatracker.trips.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Gerenciamento de viagens e cobranças")
public class TripController {

    private final TripService tripService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar uma nova viagem")
    public TripResponse registerTrip(@RequestBody @Valid TripRequest request) {
        return tripService.registerTrip(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar viagens")
    public List<TripResponse> getAllTrips(@RequestParam(required = false) String groupId) {
        return tripService.getAllTrips(groupId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar viagem por ID")
    public TripResponse getTripById(@PathVariable UUID id) {
        return tripService.getTripById(id);
    }

    @GetMapping("/debts/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar dívidas em aberto de um usuário")
    public List<TripResponse> getDebtsByUserId(@PathVariable String userId) {
        return tripService.getDebtsByUserId(userId);
    }

    @PatchMapping("/participants/{participantId}/pay")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Marcar participante como pago")
    public ParticipantResponse markParticipantAsPaid(@PathVariable UUID participantId) {
        return tripService.markParticipantAsPaid(participantId);
    }
}
