package com.uade.tpejemplo.model.interfaces;

import java.math.BigDecimal;

// Composite: interfaz comun de cuota, credito y cartera (Component)
public interface Deuda {

    BigDecimal saldo();

    BigDecimal montoVencido();
}
