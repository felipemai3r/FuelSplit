package com.caronatracker.usersgroupsservice.dto;

import com.caronatracker.usersgroupsservice.entity.GroupStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupResponse(
        @Schema(description = "ID do grupo") UUID id,
        @Schema(description = "ID do dono") UUID ownerId,
        @Schema(description = "Nome do grupo") String name,
        @Schema(description = "ID do veículo ativo") UUID activeVehicleId,
        @Schema(description = "Distância base em km") BigDecimal baseDistanceKm,
        @Schema(description = "Preço do litro de combustível") BigDecimal fuelPricePerLiter,
        @Schema(description = "Status do grupo") GroupStatus status,
        @Schema(description = "Data de criação") Instant createdAt,
        @Schema(description = "Membros do grupo com saldos") List<MemberBalanceResponse> members
) {
}
