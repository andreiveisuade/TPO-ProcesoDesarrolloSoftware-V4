package com.uade.tpejemplo.model.credito;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

// Conjunto de deudas (creditos de un cliente o de toda la financiera). No se persiste.
// Composite: suma la deuda de sus hijos sin saber si son creditos o cuotas
public final class Cartera implements Deuda {

    private final List<Deuda> deudas;

    private Cartera(List<Deuda> deudas) {
        this.deudas = deudas;
    }

    public static Cartera de(List<? extends Deuda> deudas) {
        return new Cartera(List.copyOf(deudas));
    }

    public BigDecimal saldo() {
        return sumar(Deuda::saldo);
    }

    public BigDecimal montoVencido() {
        return sumar(Deuda::montoVencido);
    }

    private BigDecimal sumar(Function<Deuda, BigDecimal> monto) {
        return deudas.stream()
            .map(monto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
