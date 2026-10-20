package com.uade.tpejemplo.dto.response;

import com.uade.tpejemplo.model.credito.ICobranza;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CobranzaResponse {

    private Long id;
    private Long idCredito;
    private Integer numeroCuota;
    private BigDecimal importe;
    private LocalDate fechaCobranza;
    private boolean anulada;

    public static CobranzaResponse desde(ICobranza cobranza) {
        return CobranzaResponse.builder()
            .id(cobranza.getId())
            .idCredito(cobranza.getCuota().getCredito().getId())
            .numeroCuota(cobranza.getCuota().getNumero())
            .importe(cobranza.getImporte())
            .fechaCobranza(cobranza.getFechaCobranza())
            .anulada(cobranza.isAnulada())
            .build();
    }
}
