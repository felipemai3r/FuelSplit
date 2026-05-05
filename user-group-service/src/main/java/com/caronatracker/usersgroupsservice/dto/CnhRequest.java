package com.caronatracker.usersgroupsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record CnhRequest(
        @Schema(description = "Número da CNH") String cnhNumber,
        @Schema(description = "Data de validade da CNH") LocalDate cnhExpiry
) {
}
