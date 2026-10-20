package com.uade.tpejemplo.model.plan;

import com.uade.tpejemplo.model.interfaces.CalculoDeCuota;

import java.math.BigDecimal;
import java.math.RoundingMode;

// La tasa es mensual sobre el capital: total = C * (1 + i * n), con i = tasa mensual
// y n = cantidad de cuotas. 10.000 al 3% mensual se devuelven 11.800 en 6 cuotas y 17.200 en 24.
// Strategy: estrategia concreta
public class InteresSimple implements CalculoDeCuota {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private BigDecimal totalADevolver(BigDecimal capital, BigDecimal tasaInteres, int cantidadCuotas) {
        BigDecimal interes = tasaInteres.divide(CIEN, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(cantidadCuotas));
        return capital.multiply(BigDecimal.ONE.add(interes)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal importeCuota(BigDecimal capital, BigDecimal tasaInteres, int cantidadCuotas) {
        return totalADevolver(capital, tasaInteres, cantidadCuotas)
            .divide(BigDecimal.valueOf(cantidadCuotas), 2, RoundingMode.HALF_UP);
    }
}
