package com.caronatracker.usersgroupsservice.event;

import java.math.BigDecimal;
import java.util.List;

public record TripRegisteredEvent(
        String tripId,
        String groupId,
        String date,
        BigDecimal totalCost,
        List<ParticipantDto> participants
) {
    public record ParticipantDto(String userId, BigDecimal amountOwed) {
    }
}
