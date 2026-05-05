package com.caronatracker.usersgroupsservice.dto;

import com.caronatracker.usersgroupsservice.entity.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record MemberBalanceResponse(
        @Schema(description = "ID do usuário") UUID userId,
        @Schema(description = "Nome do usuário") String name,
        @Schema(description = "Papel no grupo") MemberRole role,
        @Schema(description = "Saldo acumulado") BigDecimal balance
) {
}
