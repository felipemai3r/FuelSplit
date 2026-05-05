package com.caronatracker.trips.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {

    private UUID id;
    private String groupId;
    private LocalDate date;
    private Double distanceKm;
    private Double fuelConsumptionKmL;
    private Double fuelPricePerLiter;
    private Double totalCost;
    private Double costPerParticipant;
    private List<ParticipantResponse> participants;
    private LocalDateTime createdAt;
}
