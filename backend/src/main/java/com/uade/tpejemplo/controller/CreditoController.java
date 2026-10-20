package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.dto.response.CreditoResponse;
import com.uade.tpejemplo.model.usuario.IUsuario;
import com.uade.tpejemplo.service.CreditoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints de creditos: otorgar, consultar y anular.
// MVC: controlador; traduce HTTP a casos de uso y no tiene reglas de negocio
@RestController
@Tag(name = "Créditos")
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @Operation(summary = "Crear crédito con sus cuotas")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @PostMapping
    public ResponseEntity<CreditoResponse> crear(@Valid @RequestBody CreditoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creditoService.crear(request));
    }

    @Operation(summary = "Listar todos los créditos")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @GetMapping
    public ResponseEntity<List<CreditoResponse>> listarTodos() {
        return ResponseEntity.ok(creditoService.listarTodos());
    }

    @Operation(summary = "Buscar crédito por id")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<CreditoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(creditoService.buscarPorId(id));
    }

    @Operation(summary = "Listar créditos de un cliente")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @GetMapping("/cliente/{dni}")
    public ResponseEntity<List<CreditoResponse>> listarPorCliente(@PathVariable String dni) {
        return ResponseEntity.ok(creditoService.listarPorCliente(dni));
    }

    @Operation(summary = "Anular crédito")
    @ApiResponse(responseCode = "204", description = "Sin contenido")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "Sin rol o permiso")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @DeleteMapping("/anular/{id}")
    public ResponseEntity<Void> anularCredito(@PathVariable Long id, @AuthenticationPrincipal(expression = "usuario") IUsuario usuario) {
        creditoService.anularCredito(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
