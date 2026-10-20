package com.uade.tpejemplo.model.interfaces;

import com.uade.tpejemplo.model.plan.Renglon;

import java.math.BigDecimal;
import java.util.List;

// Strategy: interfaz comun del calculo de cuota
public interface CalculoDeCuota {

    BigDecimal importeCuota(BigDecimal capital, BigDecimal tasaInteres, int cantidadCuotas);

    List<Renglon> desglose(BigDecimal capital, BigDecimal tasaInteres, int cantidadCuotas);
}
