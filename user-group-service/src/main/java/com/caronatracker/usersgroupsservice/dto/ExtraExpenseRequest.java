package com.caronatracker.usersgroupsservice.dto;

import com.caronatracker.usersgroupsservice.entity.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExtraExpenseRequest(
        @Schema(description = "Descrição da despesa") @NotBlank String description,
        @Schema(description = "Valor total da despesa") @NotNull BigDecimal totalAmount,
        @Schema(description = "Tipo de divisão: EQUAL ou CUSTOM") @NotNull SplitType splitType,
        @Schema(description = "Data da despesa") @NotNull LocalDate expenseDate,
        @Schema(description = "Participantes com valor individual (obrigatório para CUSTOM)") List<ParticipantRequest> participants
) {
    public record ParticipantRequest(
            @Schema(description = "ID do usuário participante") @NotNull UUID userId,
            @Schema(description = "Valor devido pelo participante") @NotNull BigDecimal amountOwed
    ) {
    }
}
