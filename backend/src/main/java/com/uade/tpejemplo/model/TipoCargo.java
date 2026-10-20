package com.uade.tpejemplo.model;

import com.uade.tpejemplo.model.interfaces.CalculoDeCuota;
import com.uade.tpejemplo.model.plan.GastoDeAdministracion;
import com.uade.tpejemplo.model.plan.SeguroDeVida;

// Cargos opcionales que se eligen al otorgar un credito. Se guarda el nombre
// del cargo (no el decorador) y con el se vuelve a armar el calculo.
// Decorator: cada cargo sabe con que decorador concreto envolver el calculo
public enum TipoCargo {
    SEGURO_DE_VIDA {
        @Override
        public CalculoDeCuota aplicarSobre(CalculoDeCuota calculo) {
            return new SeguroDeVida(calculo);
        }
    },
    GASTO_DE_ADMINISTRACION {
        @Override
        public CalculoDeCuota aplicarSobre(CalculoDeCuota calculo) {
            return new GastoDeAdministracion(calculo);
        }
    };

    public abstract CalculoDeCuota aplicarSobre(CalculoDeCuota calculo);
}
