package com.caronatracker.usersgroupsservice.dto;

import com.caronatracker.usersgroupsservice.entity.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExtraExpenseResponse(
        @Schema(description = "ID da despesa") UUID id,
        @Schema(description = "ID do grupo") UUID groupId,
        @Schema(description = "ID do usuário que lançou") UUID createdByUserId,
        @Schema(description = "Descrição") String description,
        @Schema(description = "Valor total") BigDecimal totalAmount,
        @Schema(description = "Tipo de divisão") SplitType splitType,
        @Schema(description = "Data da despesa") LocalDate expenseDate,
        @Schema(description = "Data de criação") Instant createdAt,
        @Schema(description = "Participantes") List<ParticipantResponse> participants
) {
    public record ParticipantResponse(
            @Schema(description = "ID do usuário") UUID userId,
            @Schema(description = "Valor devido") BigDecimal amountOwed
    ) {
    }
}
