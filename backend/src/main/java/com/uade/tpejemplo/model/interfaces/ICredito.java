package com.uade.tpejemplo.model.interfaces;

import com.uade.tpejemplo.model.EstadoCredito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ICredito extends Deuda {

    Long getId();

    ICliente getCliente();

    LocalDate getFecha();

    ICondicionesDeCredito getCondiciones();

    BigDecimal getImporteCuota();

    boolean isAnulado();

    BigDecimal totalADevolver();

    List<? extends ICuota> getCuotas();

    EstadoCredito estado();

    boolean puedeAnularse();

    void anular();
}
