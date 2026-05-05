package com.caronatracker.usersgroupsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GroupRequest(
        @Schema(description = "Nome do grupo") @NotBlank String name,
        @Schema(description = "Distância base da rota em km") @NotNull BigDecimal baseDistanceKm,
        @Schema(description = "Preço do litro de combustível") @NotNull BigDecimal fuelPricePerLiter
) {
}
