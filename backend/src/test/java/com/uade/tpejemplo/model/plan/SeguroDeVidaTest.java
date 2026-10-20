package com.uade.tpejemplo.model.plan;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SeguroDeVidaTest {

    @Test
    void sumaElCeroComaUnoPorCientoDelCapitalALaCuotaDeInteresSimple() {
        // 604.17 + 10 de seguro
        assertThat(new SeguroDeVida(new InteresSimple()).importeCuota(new BigDecimal("10000"), new BigDecimal("45"), 24))
            .isEqualByComparingTo("614.17");
    }

    @Test
    void sumaElCeroComaUnoPorCientoDelCapitalALaCuotaDelSistemaFrances() {
        // 576.19 + 1 de seguro
        assertThat(new SeguroDeVida(new SistemaFrances()).importeCuota(new BigDecimal("1000"), new BigDecimal("10"), 2))
            .isEqualByComparingTo("577.19");
    }
}
