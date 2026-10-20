package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.response.ClienteResponse;
import com.uade.tpejemplo.dto.response.DeudaClienteResponse;
import com.uade.tpejemplo.service.ClienteService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints de clientes.
// MVC: controlador; traduce HTTP a casos de uso y no tiene reglas de negocio
@RestController
@Tag(name = "Clientes")
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Crear cliente")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @Operation(summary = "Buscar cliente por DNI")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @GetMapping("/{dni}")
    public ResponseEntity<ClienteResponse> buscarPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(clienteService.buscarPorDni(dni));
    }

    @Operation(summary = "Deuda del cliente: saldo pendiente y monto vencido")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @GetMapping("/{dni}/deuda")
    public ResponseEntity<DeudaClienteResponse> deuda(@PathVariable String dni) {
        return ResponseEntity.ok(clienteService.deuda(dni));
    }

    @Operation(summary = "Listar clientes")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }
}
