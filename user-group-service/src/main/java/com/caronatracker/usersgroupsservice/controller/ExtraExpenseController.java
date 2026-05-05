package com.caronatracker.usersgroupsservice.controller;

import com.caronatracker.usersgroupsservice.dto.ExtraExpenseRequest;
import com.caronatracker.usersgroupsservice.dto.ExtraExpenseResponse;
import com.caronatracker.usersgroupsservice.service.ExtraExpenseService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "Despesas Extras", description = "Gerenciamento de despesas extras dos grupos")
public class ExtraExpenseController {

    private final ExtraExpenseService extraExpenseService;
    private final UserService userService;

    @Operation(summary = "Lançar despesa extra no grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Despesa registrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou soma incorreta"),
            @ApiResponse(responseCode = "403", description = "Apenas o dono do grupo pode lançar despesas")
    })
    @PostMapping("/{id}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ExtraExpenseResponse createExpense(@PathVariable UUID id,
                                              @RequestBody @Valid ExtraExpenseRequest request) {
        UUID currentUserId = getCurrentUserId();
        return extraExpenseService.createExpense(id, currentUserId, request);
    }

    @Operation(summary = "Listar despesas extras do grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de despesas"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    })
    @GetMapping("/{id}/expenses")
    public List<ExtraExpenseResponse> listExpenses(@PathVariable UUID id) {
        return extraExpenseService.listExpenses(id);
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email).getId();
    }
}
