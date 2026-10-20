package com.uade.tpejemplo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long cantidadClientes;
    private Long cantidadCreditos;
    private BigDecimal montoTotalFinanciado;
    private BigDecimal montoTotalCobrado;
    private BigDecimal saldoPendiente;
    private BigDecimal montoVencido;
}