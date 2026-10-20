package com.uade.tpejemplo.model.plan;

import java.math.BigDecimal;

// Una linea del desglose de la cuota: que concepto suma y cuanto
public record Renglon(String concepto, BigDecimal importe) {
}
