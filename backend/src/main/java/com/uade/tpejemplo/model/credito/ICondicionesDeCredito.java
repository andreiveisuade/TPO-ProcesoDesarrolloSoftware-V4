package com.uade.tpejemplo.model.credito;

import com.uade.tpejemplo.model.plan.Renglon;
import com.uade.tpejemplo.model.plan.TipoCargo;
import com.uade.tpejemplo.model.plan.TipoPlan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface ICondicionesDeCredito {

    BigDecimal getDeudaOriginal();

    BigDecimal getTasaInteres();

    Integer getCantidadCuotas();

    TipoPlan getTipoPlan();

    Set<TipoCargo> getCargos();

    List<Renglon> desglose();
}
