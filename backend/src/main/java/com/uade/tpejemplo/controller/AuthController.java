package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.dto.request.LoginRequest;
import com.uade.tpejemplo.dto.request.RegisterRequest;
import com.uade.tpejemplo.dto.response.AuthResponse;
import com.uade.tpejemplo.security.UsuarioDetails;
import com.uade.tpejemplo.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Endpoints de login, registro y usuario autenticado.
// MVC: controlador; traduce HTTP a casos de uso y no tiene reglas de negocio
@RestController
@Tag(name = "Auth")
@SecurityRequirements
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar usuario")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @Operation(summary = "Iniciar sesión")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Usuario autenticado, con rol y permisos actuales")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> actual(@AuthenticationPrincipal UsuarioDetails usuario) {
        return ResponseEntity.ok(authService.actual(usuario.getUsername()));
    }
}
