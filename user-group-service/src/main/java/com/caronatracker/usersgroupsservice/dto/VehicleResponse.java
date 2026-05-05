package com.caronatracker.usersgroupsservice.dto;

import com.caronatracker.usersgroupsservice.entity.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
        @Schema(description = "ID do veículo") UUID id,
        @Schema(description = "ID do dono") UUID ownerId,
        @Schema(description = "Apelido") String nickname,
        @Schema(description = "Placa") String plate,
        @Schema(description = "Marca") String brand,
        @Schema(description = "Modelo") String model,
        @Schema(description = "Ano") Integer year,
        @Schema(description = "Média km/l") BigDecimal avgKmPerLiter,
        @Schema(description = "Tipo de combustível") FuelType fuelType,
        @Schema(description = "Ativo") Boolean active,
        @Schema(description = "Data de cadastro") Instant createdAt
) {
}
