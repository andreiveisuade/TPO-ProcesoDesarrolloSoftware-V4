package com.uade.tpejemplo.model.interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ICuota extends Deuda {

    Long getId();

    ICredito getCredito();

    Integer getNumero();

    BigDecimal getImporte();

    LocalDate getFechaVencimiento();

    boolean estaPagada();

    boolean estaVencida();

    ICobranza registrarCobranza(BigDecimal importe);
}
