package com.uade.tpejemplo.model.credito;

import com.uade.tpejemplo.model.cliente.ICliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ICredito extends Deuda {

    Long getId();

    ICliente getCliente();

    LocalDate getFecha();

    ICondicionesDeCredito getCondiciones();

    BigDecimal importeCuota();

    boolean isAnulado();

    BigDecimal totalADevolver();

    List<? extends ICuota> getCuotas();

    EstadoCredito estado();

    boolean puedeAnularse();

    void anular();
}
