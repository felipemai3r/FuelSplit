package com.caronatracker.trips.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripEventPayload {

    private UUID tripId;
    private String groupId;
    private LocalDate date;
    private Double totalCost;
    private List<ParticipantInfo> participants;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantInfo {
        private String userId;
        private Double amountOwed;
    }
}
