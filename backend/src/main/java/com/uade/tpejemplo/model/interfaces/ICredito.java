package com.uade.tpejemplo.model.interfaces;

import com.uade.tpejemplo.model.EstadoCredito;
import com.uade.tpejemplo.model.TipoCargo;
import com.uade.tpejemplo.model.TipoPlan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ICredito extends Deuda {

    Long getId();

    ICliente getCliente();

    BigDecimal getDeudaOriginal();

    LocalDate getFecha();

    BigDecimal getTasaInteres();

    TipoPlan getTipoPlan();

    Set<TipoCargo> getCargos();

    BigDecimal getImporteCuota();

    Integer getCantidadCuotas();

    boolean isAnulado();

    BigDecimal totalADevolver();

    List<? extends ICuota> getCuotas();

    EstadoCredito estado();

    boolean estaCancelado();

    boolean tieneCobranzas();

    boolean puedeAnularse();

    void anular();
}
