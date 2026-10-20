package com.uade.tpejemplo.model.plan;

import com.uade.tpejemplo.model.interfaces.CalculoDeCuota;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

// La tasa es mensual sobre el saldo: cuota fija = C * i / (1 - (1 + i)^-n), con i = tasaMensual.
// Con tasa 0 la cuota es el capital dividido en partes iguales.
// Strategy: estrategia concreta
public class SistemaFrances implements CalculoDeCuota {

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final MathContext PRECISION = MathContext.DECIMAL64;

    @Override
    public BigDecimal importeCuota(BigDecimal capital, BigDecimal tasaInteres, int cantidadCuotas) {
        BigDecimal tasaMensual = tasaInteres.divide(CIEN, PRECISION);
        if (tasaMensual.signum() == 0) {
            return capital.divide(BigDecimal.valueOf(cantidadCuotas), 2, RoundingMode.HALF_UP);
        }
        BigDecimal descuento = BigDecimal.ONE.subtract(
            BigDecimal.ONE.divide(BigDecimal.ONE.add(tasaMensual).pow(cantidadCuotas, PRECISION), PRECISION));
        return capital.multiply(tasaMensual).divide(descuento, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<Renglon> desglose(BigDecimal capital, BigDecimal tasaInteres, int cantidadCuotas) {
        return List.of(new Renglon("Sistema francés", importeCuota(capital, tasaInteres, cantidadCuotas)));
    }
}
