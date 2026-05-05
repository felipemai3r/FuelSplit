package com.caronatracker.usersgroupsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserRequest(
        @Schema(description = "ID do usuário no auth-service") @NotNull UUID authUserId,
        @Schema(description = "Nome completo") @NotBlank String name,
        @Schema(description = "E-mail único") @NotBlank @Email String email,
        @Schema(description = "Telefone (opcional)") String phone
) {
}
