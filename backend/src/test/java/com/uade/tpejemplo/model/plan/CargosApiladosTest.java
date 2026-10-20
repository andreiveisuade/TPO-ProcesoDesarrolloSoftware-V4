package com.uade.tpejemplo.model.plan;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CargosApiladosTest {

    private final BigDecimal capital = new BigDecimal("10000");
    private final BigDecimal tasa = new BigDecimal("45");

    @Test
    void losDosCargosSeSumanSobreLaCuotaDelPlan() {
        // 604.17 + 10 de seguro + 500 de gastos
        assertThat(new GastoDeAdministracion(new SeguroDeVida(new InteresSimple())).importeCuota(capital, tasa, 24))
            .isEqualByComparingTo("1114.17");
    }

    @Test
    void elOrdenDeLosCargosNoCambiaLaCuotaPorqueSonAditivos() {
        BigDecimal seguroAdentro = new GastoDeAdministracion(new SeguroDeVida(new SistemaFrances())).importeCuota(capital, tasa, 24);
        BigDecimal gastoAdentro = new SeguroDeVida(new GastoDeAdministracion(new SistemaFrances())).importeCuota(capital, tasa, 24);

        assertThat(seguroAdentro).isEqualByComparingTo(gastoAdentro);
    }
}
