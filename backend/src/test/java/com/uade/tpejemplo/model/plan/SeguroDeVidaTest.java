package com.uade.tpejemplo.model.plan;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SeguroDeVidaTest {

    @Test
    void sumaElCeroComaUnoPorCientoDelCapitalALaCuotaDeInteresSimple() {
        // 716.67 + 10 de seguro
        assertThat(new SeguroDeVida(new InteresSimple()).importeCuota(new BigDecimal("10000"), new BigDecimal("3"), 24))
            .isEqualByComparingTo("726.67");
    }

    @Test
    void sumaElCeroComaUnoPorCientoDelCapitalALaCuotaDelSistemaFrances() {
        // 576.19 + 1 de seguro
        assertThat(new SeguroDeVida(new SistemaFrances()).importeCuota(new BigDecimal("1000"), new BigDecimal("10"), 2))
            .isEqualByComparingTo("577.19");
    }
}
