package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.dto.request.CobranzaRequest;
import com.uade.tpejemplo.dto.response.CobranzaResponse;
import com.uade.tpejemplo.model.interfaces.IUsuario;
import com.uade.tpejemplo.service.CobranzaService;
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

// Endpoints de cobranzas: registrar, listar y anular.
// MVC: controlador; traduce HTTP a casos de uso y no tiene reglas de negocio
@RestController
@Tag(name = "Cobranzas")
@RequestMapping("/api/cobranzas")
@RequiredArgsConstructor
public class CobranzaController {

    private final CobranzaService cobranzaService;

    @Operation(summary = "Registrar cobranza de una cuota")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @PostMapping
    public ResponseEntity<CobranzaResponse> registrar(@Valid @RequestBody CobranzaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cobranzaService.registrar(request));
    }

    @Operation(summary = "Listar cobranzas de un crédito")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @GetMapping("/credito/{idCredito}")
    public ResponseEntity<List<CobranzaResponse>> listarPorCredito(@PathVariable Long idCredito) {
        return ResponseEntity.ok(cobranzaService.listarPorCredito(idCredito));
    }

    @Operation(summary = "Anular cobranza del día")
    @ApiResponse(responseCode = "204", description = "Sin contenido")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "Sin rol o permiso")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anularCobranza(@PathVariable Long id, @AuthenticationPrincipal(expression = "usuario") IUsuario usuario) {
        cobranzaService.anularCobranza(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
