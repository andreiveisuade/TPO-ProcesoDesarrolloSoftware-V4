package com.uade.tpejemplo.model.credito;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ICobranza {

    Long getId();

    ICuota getCuota();

    BigDecimal getImporte();

    LocalDate getFechaCobranza();

    boolean isAnulada();

    void anular();
}
