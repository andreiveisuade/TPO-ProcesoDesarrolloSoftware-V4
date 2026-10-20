package com.uade.tpejemplo.dto.response;

import com.uade.tpejemplo.model.EstadoCredito;
import com.uade.tpejemplo.model.TipoCargo;
import com.uade.tpejemplo.model.TipoPlan;
import com.uade.tpejemplo.model.interfaces.ICondicionesDeCredito;
import com.uade.tpejemplo.model.interfaces.ICredito;
import com.uade.tpejemplo.model.plan.Renglon;
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
    private List<Renglon> desgloseCuota;
    private Integer cantidadCuotas;
    private List<CuotaResponse> cuotas;
    private boolean anulado;
    private EstadoCredito estado;
    private BigDecimal saldo;
    private boolean puedeAnularse;

    public static CreditoResponse desde(ICredito credito) {
        ICondicionesDeCredito condiciones = credito.getCondiciones();
        return CreditoResponse.builder()
            .id(credito.getId())
            .dniCliente(credito.getCliente().getDni())
            .nombreCliente(credito.getCliente().getNombre())
            .deudaOriginal(condiciones.getDeudaOriginal())
            .fecha(credito.getFecha())
            .tasaInteres(condiciones.getTasaInteres())
            .tipoPlan(condiciones.getTipoPlan())
            .cargos(Set.copyOf(condiciones.getCargos()))
            .totalADevolver(credito.totalADevolver())
            .importeCuota(credito.importeCuota())
            .desgloseCuota(condiciones.desglose())
            .cantidadCuotas(condiciones.getCantidadCuotas())
            .cuotas(credito.getCuotas().stream().map(CuotaResponse::desde).toList())
            .anulado(credito.isAnulado())
            .estado(credito.estado())
            .saldo(credito.saldo())
            .puedeAnularse(credito.puedeAnularse())
            .build();
    }
}
