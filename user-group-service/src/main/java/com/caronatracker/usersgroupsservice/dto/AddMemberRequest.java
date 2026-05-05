package com.caronatracker.usersgroupsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(
        @Schema(description = "ID do usuário a ser adicionado") @NotNull UUID userId
) {
}
