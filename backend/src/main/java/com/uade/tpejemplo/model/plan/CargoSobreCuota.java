package com.uade.tpejemplo.model.plan;

import com.uade.tpejemplo.model.interfaces.CalculoDeCuota;

import java.math.BigDecimal;

// Cargo que se suma a la cuota sin tocar el sistema de amortizacion.
// Decorator: envuelve un CalculoDeCuota, delega en el y le suma su cargo
public abstract class CargoSobreCuota implements CalculoDeCuota {

    private final CalculoDeCuota envuelto;

    protected CargoSobreCuota(CalculoDeCuota envuelto) {
        this.envuelto = envuelto;
    }

    @Override
    public BigDecimal importeCuota(BigDecimal capital, BigDecimal tasaInteres, int cantidadCuotas) {
        return envuelto.importeCuota(capital, tasaInteres, cantidadCuotas).add(cargo(capital, cantidadCuotas));
    }

    protected abstract BigDecimal cargo(BigDecimal capital, int cantidadCuotas);
}
