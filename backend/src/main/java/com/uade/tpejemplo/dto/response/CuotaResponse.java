package com.uade.tpejemplo.dto.response;

import com.uade.tpejemplo.model.credito.ICuota;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CuotaResponse {

    private Long idCredito;
    private Integer numeroCuota;
    private BigDecimal importe;
    private LocalDate fechaVencimiento;
    private boolean pagada;
    private boolean vencida;

    public static CuotaResponse desde(ICuota cuota) {
        return new CuotaResponse(
            cuota.getCredito().getId(),
            cuota.getNumero(),
            cuota.getImporte(),
            cuota.getFechaVencimiento(),
            cuota.estaPagada(),
            cuota.estaVencida()
        );
    }
}
