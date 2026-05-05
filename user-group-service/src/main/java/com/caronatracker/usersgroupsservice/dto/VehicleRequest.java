package com.caronatracker.usersgroupsservice.dto;

import com.caronatracker.usersgroupsservice.entity.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VehicleRequest(
        @Schema(description = "Apelido do veículo") @NotBlank String nickname,
        @Schema(description = "Placa") @NotBlank String plate,
        @Schema(description = "Marca") @NotBlank String brand,
        @Schema(description = "Modelo") @NotBlank String model,
        @Schema(description = "Ano") @NotNull Integer year,
        @Schema(description = "Média de consumo km/l") @NotNull BigDecimal avgKmPerLiter,
        @Schema(description = "Tipo de combustível") @NotNull FuelType fuelType
) {
}
