package com.uade.tpejemplo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.uade.tpejemplo.dto.request.ActivoRequest;
import com.uade.tpejemplo.dto.request.AltaUsuarioRequest;
import com.uade.tpejemplo.dto.request.PermisosRequest;
import com.uade.tpejemplo.dto.request.RolRequest;
import com.uade.tpejemplo.dto.response.UsuarioResponse;
import com.uade.tpejemplo.service.AdminService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

// Endpoints del ADMIN para gestionar usuarios, roles y permisos.
// MVC: controlador; traduce HTTP a casos de uso y no tiene reglas de negocio
@RestController
@Tag(name = "Admin")
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Listar usuarios")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "Sin rol o permiso")
    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return adminService.listarUsuarios();
    }

    @Operation(summary = "Dar de alta un usuario")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "Sin rol o permiso")
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody AltaUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.crearUsuario(request));
    }

    @Operation(summary = "Actualizar permisos de anulación")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "Sin rol o permiso")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @PutMapping("/usuarios/{id}/permisos")
    public UsuarioResponse actualizarPermisos(@PathVariable Long id, @Valid @RequestBody PermisosRequest request) {
        return adminService.actualizarPermisos(id, request);
    }

    @Operation(summary = "Cambiar rol de un usuario")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "Sin rol o permiso")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @PutMapping("/usuarios/{id}/rol")
    public UsuarioResponse actualizarRol(@PathVariable Long id, @Valid @RequestBody RolRequest request) {
        return adminService.actualizarRol(id, request);
    }

    @Operation(summary = "Desactivar o reactivar un usuario")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "Sin rol o permiso")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @PutMapping("/usuarios/{id}/activo")
    public UsuarioResponse actualizarActivo(@PathVariable Long id, @Valid @RequestBody ActivoRequest request) {
        return adminService.actualizarActivo(id, request);
    }
}
