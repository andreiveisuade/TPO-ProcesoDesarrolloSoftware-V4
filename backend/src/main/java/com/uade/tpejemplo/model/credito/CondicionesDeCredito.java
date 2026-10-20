package com.uade.tpejemplo.model.credito;

import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.model.plan.CalculoDeCuota;
import com.uade.tpejemplo.model.plan.Renglon;
import com.uade.tpejemplo.model.plan.TipoCargo;
import com.uade.tpejemplo.model.plan.TipoPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// Lo que se pacta al otorgar un credito (capital, tasa mensual, cuotas, plan y cargos)
// y cuanto vale la cuota con esas condiciones. No cambia despues de otorgado.
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CondicionesDeCredito implements ICondicionesDeCredito {

    @NotNull
    @Column(name = "deuda_original", nullable = false, precision = 12, scale = 2)
    private BigDecimal deudaOriginal;

    @NotNull
    @Column(name = "tasa_interes", nullable = false, precision = 5, scale = 2)
    private BigDecimal tasaInteres;

    @Min(1)
    @Column(name = "cantidad_cuotas", nullable = false)
    private Integer cantidadCuotas;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_plan", nullable = false, columnDefinition = "varchar(20) default 'INTERES_SIMPLE'")
    private TipoPlan tipoPlan;

    // EnumSet: al otorgar, los cargos se aplican siempre en el orden del enum
    @ElementCollection
    @CollectionTable(name = "credito_cargos", joinColumns = @JoinColumn(name = "id_credito"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cargo", nullable = false, length = 30)
    private Set<TipoCargo> cargos = EnumSet.noneOf(TipoCargo.class);

    public CondicionesDeCredito(BigDecimal deudaOriginal, BigDecimal tasaInteres, Integer cantidadCuotas,
                                TipoPlan tipoPlan, Set<TipoCargo> cargos) {
        // Objeto de valor: nace valido o no nace
        if (deudaOriginal.signum() <= 0) {
            throw new BusinessException("El capital debe ser mayor a cero");
        }
        if (cantidadCuotas < 1) {
            throw new BusinessException("Debe tener al menos 1 cuota");
        }
        if (tasaInteres.signum() < 0) {
            throw new BusinessException("La tasa de interes no puede ser negativa");
        }
        if (tipoPlan == null) {
            throw new BusinessException("El tipo de plan es obligatorio");
        }
        this.deudaOriginal = deudaOriginal;
        this.tasaInteres = tasaInteres;
        this.cantidadCuotas = cantidadCuotas;
        this.tipoPlan = tipoPlan;
        this.cargos.addAll(cargos);
    }

    // Solo para que Credito genere su plan: despues el importe vive en cada cuota
    BigDecimal importeCuota() {
        return calculo().importeCuota(deudaOriginal, tasaInteres, cantidadCuotas);
    }

    // Explica la cuota renglon por renglon; el importe guardado en cada cuota sigue siendo el hecho
    public List<Renglon> desglose() {
        return calculo().desglose(deudaOriginal, tasaInteres, cantidadCuotas);
    }

    // Strategy: el contexto delega el cálculo de la cuota en la estrategia elegida al otorgar (no cambia después)
    // Decorator: cada cargo del credito envuelve a la estrategia y le suma lo suyo
    private CalculoDeCuota calculo() {
        CalculoDeCuota calculo = tipoPlan.calculo();
        for (TipoCargo cargo : cargos) {
            calculo = cargo.aplicarSobre(calculo);
        }
        return calculo;
    }

    public Set<TipoCargo> getCargos() {
        return Collections.unmodifiableSet(cargos);
    }
}
