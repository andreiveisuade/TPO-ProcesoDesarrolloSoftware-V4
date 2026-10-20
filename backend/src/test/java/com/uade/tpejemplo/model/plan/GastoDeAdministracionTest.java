package com.uade.tpejemplo.model.plan;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GastoDeAdministracionTest {

    @Test
    void sumaQuinientosALaCuotaDeInteresSimple() {
        assertThat(new GastoDeAdministracion(new InteresSimple()).importeCuota(new BigDecimal("10000"), new BigDecimal("45"), 24))
            .isEqualByComparingTo("1104.17");
    }

    @Test
    void sumaQuinientosALaCuotaDelSistemaFrances() {
        assertThat(new GastoDeAdministracion(new SistemaFrances()).importeCuota(new BigDecimal("1000"), new BigDecimal("10"), 2))
            .isEqualByComparingTo("1076.19");
    }
}
