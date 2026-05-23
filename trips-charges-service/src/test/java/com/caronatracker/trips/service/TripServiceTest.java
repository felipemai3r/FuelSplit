package com.caronatracker.trips.service;

import com.caronatracker.trips.domain.model.Trip;
import com.caronatracker.trips.domain.model.TripParticipant;
import com.caronatracker.trips.dto.ParticipantResponse;
import com.caronatracker.trips.dto.TripRequest;
import com.caronatracker.trips.dto.TripResponse;
import com.caronatracker.trips.exception.BusinessException;
import com.caronatracker.trips.messaging.TripEventPublisher;
import com.caronatracker.trips.repository.TripParticipantRepository;
import com.caronatracker.trips.repository.TripRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripParticipantRepository tripParticipantRepository;

    @Mock
    private TripEventPublisher tripEventPublisher;

    @InjectMocks
    private TripService tripService;

    private TripRequest buildValidRequest(String date, List<String> participantIds) {
        return TripRequest.builder()
                .groupId("group-1")
                .date(date)
                .distanceKm(100.0)
                .fuelConsumptionKmL(10.0)
                .fuelPricePerLiter(5.0)
                .participantIds(participantIds)
                .build();
    }

    @Test
    void deve_calcularCustoCorretamente_quando_parametrosValidos() {
        TripRequest request = buildValidRequest(
                LocalDate.now().toString(),
                List.of("user-1", "user-2")
        );

        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        TripResponse response = tripService.registerTrip(request);

        assertThat(response.getTotalCost()).isEqualTo(50.0);
        assertThat(response.getCostPerParticipant()).isEqualTo(25.0);
    }

    @Test
    void deve_lancarException_quando_dataForFutura() {
        TripRequest request = buildValidRequest(
                LocalDate.now().plusDays(1).toString(),
                List.of("user-1")
        );

        assertThrows(BusinessException.class, () -> tripService.registerTrip(request));
        verify(tripRepository, never()).save(any());
    }

    @Test
    void deve_lancarException_quando_listaDePasageirosVazia() {
        TripRequest request = buildValidRequest(LocalDate.now().toString(), new ArrayList<>());

        assertThrows(BusinessException.class, () -> tripService.registerTrip(request));
    }

    @Test
    void deve_registrarViagem_e_publicarEvento_quando_dadosValidos() {
        TripRequest request = buildValidRequest(
                LocalDate.now().toString(),
                List.of("user-1", "user-2")
        );

        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        TripResponse response = tripService.registerTrip(request);

        verify(tripEventPublisher, times(1)).publishTripRegistered(any());
        assertThat(response.getTotalCost()).isEqualTo(50.0);
    }

    @Test
    void deve_retornarListaVazia_quando_nenhumaViagemCadastrada() {
        when(tripRepository.findAll()).thenReturn(List.of());

        List<TripResponse> result = tripService.getAllTrips(null);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void deve_lancarException_quando_viagemNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(tripRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> tripService.getTripById(id));
    }

    @Test
    void deve_marcarParticipanteComoPago_quando_participanteExiste() {
        UUID participantId = UUID.randomUUID();

        Trip trip = Trip.builder()
                .id(UUID.randomUUID())
                .groupId("group-1")
                .date(LocalDate.now())
                .distanceKm(100.0)
                .fuelConsumptionKmL(10.0)
                .fuelPricePerLiter(5.0)
                .totalCost(50.0)
                .participants(new ArrayList<>())
                .build();

        TripParticipant participant = TripParticipant.builder()
                .id(participantId)
                .trip(trip)
                .userId("user-1")
                .amountOwed(25.0)
                .paid(false)
                .build();

        when(tripParticipantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(tripParticipantRepository.save(any(TripParticipant.class))).thenAnswer(inv -> inv.getArgument(0));

        ParticipantResponse response = tripService.markParticipantAsPaid(participantId);

        assertThat(response.isPaid()).isTrue();
        verify(tripParticipantRepository, times(1)).save(any(TripParticipant.class));
    }
}
