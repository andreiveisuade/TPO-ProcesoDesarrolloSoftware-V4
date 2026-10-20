package com.uade.tpejemplo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DeudaClienteResponse {

    private String dni;
    private BigDecimal saldo;
    private BigDecimal montoVencido;
}
