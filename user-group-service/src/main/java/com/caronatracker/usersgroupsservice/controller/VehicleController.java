package com.caronatracker.usersgroupsservice.controller;

import com.caronatracker.usersgroupsservice.dto.VehicleRequest;
import com.caronatracker.usersgroupsservice.dto.VehicleResponse;
import com.caronatracker.usersgroupsservice.service.UserService;
import com.caronatracker.usersgroupsservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Gerenciamento de veículos dos usuários")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserService userService;

    @Operation(summary = "Cadastrar veículo para o usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veículo cadastrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping("/users/{id}/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@PathVariable UUID id, @RequestBody @Valid VehicleRequest request) {
        return vehicleService.createVehicle(id, request);
    }

    @Operation(summary = "Listar veículos do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de veículos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/users/{id}/vehicles")
    public List<VehicleResponse> listVehicles(@PathVariable UUID id) {
        return vehicleService.listVehicles(id);
    }

    @Operation(summary = "Atualizar dados do veículo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo atualizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    @PutMapping("/vehicles/{id}")
    public VehicleResponse updateVehicle(@PathVariable UUID id, @RequestBody @Valid VehicleRequest request) {
        UUID currentUserId = getCurrentUserId();
        return vehicleService.updateVehicle(id, currentUserId, request);
    }

    @Operation(summary = "Ativar ou desativar veículo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    @PatchMapping("/vehicles/{id}/status")
    public VehicleResponse setVehicleStatus(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        UUID currentUserId = getCurrentUserId();
        return vehicleService.setVehicleStatus(id, currentUserId, active);
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email).getId();
    }
}
