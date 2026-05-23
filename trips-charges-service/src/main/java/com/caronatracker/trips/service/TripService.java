package com.caronatracker.trips.service;

import com.caronatracker.trips.domain.model.Trip;
import com.caronatracker.trips.domain.model.TripParticipant;
import com.caronatracker.trips.dto.*;
import com.caronatracker.trips.exception.BusinessException;
import com.caronatracker.trips.messaging.TripEventPublisher;
import com.caronatracker.trips.repository.TripParticipantRepository;
import com.caronatracker.trips.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripParticipantRepository tripParticipantRepository;
    private final TripEventPublisher tripEventPublisher;

    public TripResponse registerTrip(TripRequest request) {
        LocalDate date = LocalDate.parse(request.getDate());

        // REGRA 1: Data não pode ser futura
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException("Trip date cannot be in the future", HttpStatus.BAD_REQUEST);
        }

        if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
            throw new BusinessException("Participant list cannot be empty", HttpStatus.BAD_REQUEST);
        }

        // REGRA 2: Calcular custo total
        double rawCost = (request.getDistanceKm() / request.getFuelConsumptionKmL()) * request.getFuelPricePerLiter();
        double totalCost = BigDecimal.valueOf(rawCost)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        // REGRA 3: Custo por participante
        double costPerParticipant = totalCost / request.getParticipantIds().size();

        Trip trip = Trip.builder()
                .groupId(request.getGroupId())
                .date(date)
                .distanceKm(request.getDistanceKm())
                .fuelConsumptionKmL(request.getFuelConsumptionKmL())
                .fuelPricePerLiter(request.getFuelPricePerLiter())
                .totalCost(totalCost)
                .build();

        List<TripParticipant> participants = request.getParticipantIds().stream()
                .map(userId -> TripParticipant.builder()
                        .trip(trip)
                        .userId(userId)
                        .amountOwed(costPerParticipant)
                        .paid(false)
                        .build())
                .collect(Collectors.toList());

        trip.setParticipants(participants);

        Trip savedTrip = tripRepository.save(trip);

        tripEventPublisher.publishTripRegistered(buildEventPayload(savedTrip));

        return buildTripResponse(savedTrip);
    }

    public List<TripResponse> getAllTrips(String groupId) {
        List<Trip> trips = (groupId != null)
                ? tripRepository.findByGroupId(groupId)
                : tripRepository.findAll();

        return trips.stream().map(this::buildTripResponse).collect(Collectors.toList());
    }

    public TripResponse getTripById(UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Trip not found", HttpStatus.NOT_FOUND));
        return buildTripResponse(trip);
    }

    public List<TripResponse> getDebtsByUserId(String userId) {
        return tripParticipantRepository.findByUserIdAndPaidFalse(userId).stream()
                .map(p -> buildTripResponse(p.getTrip()))
                .collect(Collectors.toList());
    }

    public ParticipantResponse markParticipantAsPaid(UUID participantId) {
        TripParticipant participant = tripParticipantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException("Participant not found", HttpStatus.NOT_FOUND));
        participant.setPaid(true);
        TripParticipant saved = tripParticipantRepository.save(participant);
        return buildParticipantResponse(saved);
    }

    private TripResponse buildTripResponse(Trip trip) {
        int size = (trip.getParticipants() != null) ? trip.getParticipants().size() : 0;
        double costPerParticipant = (size > 0) ? trip.getTotalCost() / size : 0.0;

        List<ParticipantResponse> participantResponses = (trip.getParticipants() != null)
                ? trip.getParticipants().stream().map(this::buildParticipantResponse).collect(Collectors.toList())
                : List.of();

        return TripResponse.builder()
                .id(trip.getId())
                .groupId(trip.getGroupId())
                .date(trip.getDate())
                .distanceKm(trip.getDistanceKm())
                .fuelConsumptionKmL(trip.getFuelConsumptionKmL())
                .fuelPricePerLiter(trip.getFuelPricePerLiter())
                .totalCost(trip.getTotalCost())
                .costPerParticipant(costPerParticipant)
                .participants(participantResponses)
                .createdAt(trip.getCreatedAt())
                .build();
    }

    private ParticipantResponse buildParticipantResponse(TripParticipant p) {
        return ParticipantResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .amountOwed(p.getAmountOwed())
                .paid(p.isPaid())
                .build();
    }

    private TripEventPayload buildEventPayload(Trip trip) {
        List<TripEventPayload.ParticipantInfo> infos = trip.getParticipants().stream()
                .map(p -> TripEventPayload.ParticipantInfo.builder()
                        .userId(p.getUserId())
                        .amountOwed(p.getAmountOwed())
                        .build())
                .collect(Collectors.toList());

        return TripEventPayload.builder()
                .tripId(trip.getId())
                .groupId(trip.getGroupId())
                .date(trip.getDate())
                .totalCost(trip.getTotalCost())
                .participants(infos)
                .build();
    }
}
