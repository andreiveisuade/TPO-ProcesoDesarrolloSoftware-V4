package com.uade.tpejemplo.model.plan;

import com.uade.tpejemplo.model.interfaces.CalculoDeCuota;

import java.math.BigDecimal;

// Gasto de administracion: monto fijo en cada cuota.
// Decorator: decorador concreto
public class GastoDeAdministracion extends CargoSobreCuota {

    private static final BigDecimal MONTO_POR_CUOTA = new BigDecimal("500.00");

    public GastoDeAdministracion(CalculoDeCuota envuelto) {
        super(envuelto);
    }

    @Override
    protected String nombre() {
        return "Gastos de administración";
    }

    @Override
    protected BigDecimal cargo(BigDecimal capital, int cantidadCuotas) {
        return MONTO_POR_CUOTA;
    }
}
