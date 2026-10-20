package com.uade.tpejemplo.model.plan;

import com.uade.tpejemplo.model.interfaces.CalculoDeCuota;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Seguro de vida: 0,1% del capital en cada cuota. Simplificacion: el seguro
// real se calcula sobre el saldo deudor, no sobre el capital.
// Decorator: decorador concreto
public class SeguroDeVida extends CargoSobreCuota {

    private static final BigDecimal PORCENTAJE_POR_CUOTA = new BigDecimal("0.001");

    public SeguroDeVida(CalculoDeCuota envuelto) {
        super(envuelto);
    }

    @Override
    protected String nombre() {
        return "Seguro de vida";
    }

    @Override
    protected BigDecimal cargo(BigDecimal capital, int cantidadCuotas) {
        return capital.multiply(PORCENTAJE_POR_CUOTA).setScale(2, RoundingMode.HALF_UP);
    }
}
