package com.uade.tpejemplo.model;

import com.uade.tpejemplo.model.interfaces.CalculoDeCuota;
import com.uade.tpejemplo.model.plan.GastoDeAdministracion;
import com.uade.tpejemplo.model.plan.SeguroDeVida;

import java.util.function.UnaryOperator;

// Cargos opcionales que se eligen al otorgar un credito.
// Decorator: cada cargo envuelve el calculo con su decorador concreto
public enum TipoCargo {
    SEGURO_DE_VIDA(SeguroDeVida::new),
    GASTO_DE_ADMINISTRACION(GastoDeAdministracion::new);

    private final UnaryOperator<CalculoDeCuota> decorador;

    TipoCargo(UnaryOperator<CalculoDeCuota> decorador) {
        this.decorador = decorador;
    }

    public CalculoDeCuota aplicarSobre(CalculoDeCuota calculo) {
        return decorador.apply(calculo);
    }
}
