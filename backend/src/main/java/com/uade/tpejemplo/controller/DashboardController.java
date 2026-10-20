package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.dto.response.DashboardStatsResponse;
import com.uade.tpejemplo.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint de estadisticas del dashboard.
// MVC: controlador; traduce HTTP a casos de uso y no tiene reglas de negocio
@RestController
@Tag(name = "Dashboard")
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Estadísticas generales")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "Sin rol o permiso")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(dashboardService.obtenerEstadisticasGenerales());
    }
}