package com.caronatracker.trips.controller;

import com.caronatracker.trips.dto.ParticipantResponse;
import com.caronatracker.trips.dto.TripRequest;
import com.caronatracker.trips.dto.TripResponse;
import com.caronatracker.trips.exception.BusinessException;
import com.caronatracker.trips.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripService tripService;

    @Autowired
    private ObjectMapper objectMapper;

    private TripRequest buildValidRequest() {
        return TripRequest.builder()
                .groupId("group-1")
                .date("2024-01-15")
                .distanceKm(100.0)
                .fuelConsumptionKmL(10.0)
                .fuelPricePerLiter(5.0)
                .participantIds(List.of("user-1", "user-2"))
                .build();
    }

    private TripResponse buildTripResponse(UUID id) {
        return TripResponse.builder()
                .id(id)
                .groupId("group-1")
                .date(LocalDate.of(2024, 1, 15))
                .distanceKm(100.0)
                .fuelConsumptionKmL(10.0)
                .fuelPricePerLiter(5.0)
                .totalCost(50.0)
                .costPerParticipant(25.0)
                .participants(List.of())
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();
    }

    @Test
    void deve_retornar201_quando_viagemRegistradaComSucesso() throws Exception {
        UUID tripId = UUID.randomUUID();
        TripRequest request = buildValidRequest();
        TripResponse response = buildTripResponse(tripId);

        when(tripService.registerTrip(any(TripRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId.toString()));
    }

    @Test
    void deve_retornar400_quando_dataForFutura() throws Exception {
        TripRequest request = TripRequest.builder()
                .groupId("group-1")
                .date(LocalDate.now().plusDays(1).toString())
                .distanceKm(100.0)
                .fuelConsumptionKmL(10.0)
                .fuelPricePerLiter(5.0)
                .participantIds(List.of("user-1"))
                .build();

        when(tripService.registerTrip(any(TripRequest.class)))
                .thenThrow(new BusinessException("Trip date cannot be in the future", HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deve_retornar200_e_listaDeViagens_quando_getTrips() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<TripResponse> responses = List.of(buildTripResponse(id1), buildTripResponse(id2));

        when(tripService.getAllTrips(isNull())).thenReturn(responses);

        mockMvc.perform(get("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deve_retornar404_quando_viagemNaoEncontrada() throws Exception {
        UUID id = UUID.randomUUID();

        when(tripService.getTripById(id))
                .thenThrow(new BusinessException("Trip not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/trips/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deve_retornar200_quando_marcarParticipanteComoPago() throws Exception {
        UUID participantId = UUID.randomUUID();
        ParticipantResponse response = ParticipantResponse.builder()
                .id(participantId)
                .userId("user-1")
                .amountOwed(25.0)
                .paid(true)
                .build();

        when(tripService.markParticipantAsPaid(participantId)).thenReturn(response);

        mockMvc.perform(patch("/api/trips/participants/{id}/pay", participantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true));
    }
}
