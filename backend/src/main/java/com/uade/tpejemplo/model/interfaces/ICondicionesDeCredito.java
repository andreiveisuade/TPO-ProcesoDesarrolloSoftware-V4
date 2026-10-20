package com.uade.tpejemplo.model.interfaces;

import com.uade.tpejemplo.model.TipoCargo;
import com.uade.tpejemplo.model.TipoPlan;

import java.math.BigDecimal;
import java.util.Set;

public interface ICondicionesDeCredito {

    BigDecimal getDeudaOriginal();

    BigDecimal getTasaInteres();

    Integer getCantidadCuotas();

    TipoPlan getTipoPlan();

    Set<TipoCargo> getCargos();
}
