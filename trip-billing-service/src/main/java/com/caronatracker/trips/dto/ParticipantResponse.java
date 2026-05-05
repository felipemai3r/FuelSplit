package com.caronatracker.trips.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantResponse {

    private UUID id;
    private String userId;
    private Double amountOwed;
    private boolean paid;
}
