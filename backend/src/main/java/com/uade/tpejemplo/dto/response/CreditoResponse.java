package com.uade.tpejemplo.dto.response;

import com.uade.tpejemplo.model.EstadoCredito;
import com.uade.tpejemplo.model.TipoCargo;
import com.uade.tpejemplo.model.TipoPlan;
import com.uade.tpejemplo.model.interfaces.ICredito;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreditoResponse {

    private Long id;
    private String dniCliente;
    private String nombreCliente;
    private BigDecimal deudaOriginal;
    private LocalDate fecha;
    private BigDecimal tasaInteres;
    private TipoPlan tipoPlan;
    private Set<TipoCargo> cargos;
    private BigDecimal totalADevolver;
    private BigDecimal importeCuota;
    private Integer cantidadCuotas;
    private List<CuotaResponse> cuotas;
    private boolean anulado;
    private EstadoCredito estado;
    private BigDecimal saldo;
    private boolean puedeAnularse;

    public static CreditoResponse desde(ICredito credito) {
        return CreditoResponse.builder()
            .id(credito.getId())
            .dniCliente(credito.getCliente().getDni())
            .nombreCliente(credito.getCliente().getNombre())
            .deudaOriginal(credito.getDeudaOriginal())
            .fecha(credito.getFecha())
            .tasaInteres(credito.getTasaInteres())
            .tipoPlan(credito.getTipoPlan())
            .cargos(Set.copyOf(credito.getCargos()))
            .totalADevolver(credito.totalADevolver())
            .importeCuota(credito.getImporteCuota())
            .cantidadCuotas(credito.getCantidadCuotas())
            .cuotas(credito.getCuotas().stream().map(CuotaResponse::desde).toList())
            .anulado(credito.isAnulado())
            .estado(credito.estado())
            .saldo(credito.saldo())
            .puedeAnularse(credito.puedeAnularse())
            .build();
    }
}
