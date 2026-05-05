package com.caronatracker.usersgroupsservice.controller;

import com.caronatracker.usersgroupsservice.dto.AddMemberRequest;
import com.caronatracker.usersgroupsservice.dto.GroupRequest;
import com.caronatracker.usersgroupsservice.dto.GroupResponse;
import com.caronatracker.usersgroupsservice.service.GroupService;
import com.caronatracker.usersgroupsservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "Grupos", description = "Gerenciamento de grupos de rotina")
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    @Operation(summary = "Criar grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grupo criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@RequestBody @Valid GroupRequest request) {
        UUID currentUserId = getCurrentUserId();
        return groupService.createGroup(currentUserId, request);
    }

    @Operation(summary = "Consultar grupo com membros e saldos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo encontrado"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    })
    @GetMapping("/{id}")
    public GroupResponse getGroup(@PathVariable UUID id) {
        return groupService.getGroup(id);
    }

    @Operation(summary = "Atualizar parâmetros do grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo atualizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    })
    @PutMapping("/{id}")
    public GroupResponse updateGroup(@PathVariable UUID id, @RequestBody @Valid GroupRequest request) {
        UUID currentUserId = getCurrentUserId();
        return groupService.updateGroup(id, currentUserId, request);
    }

    @Operation(summary = "Adicionar membro ao grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro adicionado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Usuário já é membro do grupo")
    })
    @PostMapping("/{id}/members")
    public GroupResponse addMember(@PathVariable UUID id, @RequestBody @Valid AddMemberRequest request) {
        UUID currentUserId = getCurrentUserId();
        return groupService.addMember(id, currentUserId, request.userId());
    }

    @Operation(summary = "Remover membro do grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membro removido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    })
    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        UUID currentUserId = getCurrentUserId();
        groupService.removeMember(id, currentUserId, userId);
    }

    @Operation(summary = "Definir veículo ativo do grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo ativo definido"),
            @ApiResponse(responseCode = "400", description = "Veículo inativo"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Grupo ou veículo não encontrado")
    })
    @PutMapping("/{id}/vehicle")
    public GroupResponse setActiveVehicle(@PathVariable UUID id, @RequestBody Map<String, UUID> body) {
        UUID vehicleId = body.get("vehicleId");
        UUID currentUserId = getCurrentUserId();
        return groupService.setActiveVehicle(id, currentUserId, vehicleId);
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email).getId();
    }
}
