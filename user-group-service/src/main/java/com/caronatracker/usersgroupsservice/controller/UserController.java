package com.caronatracker.usersgroupsservice.controller;

import com.caronatracker.usersgroupsservice.dto.CnhRequest;
import com.caronatracker.usersgroupsservice.dto.UserRequest;
import com.caronatracker.usersgroupsservice.dto.UserResponse;
import com.caronatracker.usersgroupsservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de perfis de usuário")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Criar perfil de usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody @Valid UserRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "Consultar perfil de usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @Operation(summary = "Atualizar dados de perfil")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable UUID id, @RequestBody @Valid UserRequest request) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "Cadastrar ou atualizar CNH")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CNH atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados da CNH inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}/cnh")
    public UserResponse updateCnh(@PathVariable UUID id, @RequestBody CnhRequest request) {
        return userService.updateCnh(id, request);
    }
}
