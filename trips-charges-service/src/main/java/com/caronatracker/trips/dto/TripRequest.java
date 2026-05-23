package com.caronatracker.trips.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {

    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "Date is required")
    private String date;

    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be positive")
    private Double distanceKm;

    @NotNull(message = "Fuel consumption is required")
    @Positive(message = "Fuel consumption must be positive")
    private Double fuelConsumptionKmL;

    @NotNull(message = "Fuel price is required")
    @Positive(message = "Fuel price must be positive")
    private Double fuelPricePerLiter;

    @NotEmpty(message = "Participant list is required")
    private List<String> participantIds;
}
