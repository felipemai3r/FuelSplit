package com.caronatracker.usersgroupsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
        @Schema(description = "ID do perfil") UUID id,
        @Schema(description = "ID no auth-service") UUID authUserId,
        @Schema(description = "Nome completo") String name,
        @Schema(description = "E-mail") String email,
        @Schema(description = "Telefone") String phone,
        @Schema(description = "Possui CNH válida") Boolean hasCnh,
        @Schema(description = "Número da CNH") String cnhNumber,
        @Schema(description = "Validade da CNH") LocalDate cnhExpiry,
        @Schema(description = "Data de criação") Instant createdAt
) {
}
