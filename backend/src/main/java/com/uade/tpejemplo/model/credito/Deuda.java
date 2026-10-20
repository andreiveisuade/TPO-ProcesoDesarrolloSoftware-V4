package com.uade.tpejemplo.model.credito;

import java.math.BigDecimal;

// Composite: interfaz comun de cuota, credito y cartera (Component)
public interface Deuda {

    BigDecimal saldo();

    BigDecimal montoVencido();

    // La regla de mora la decide el modelo, no el servicio
    default boolean tieneMora() {
        return montoVencido().signum() > 0;
    }
}
