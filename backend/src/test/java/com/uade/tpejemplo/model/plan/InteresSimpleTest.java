package com.uade.tpejemplo.model.plan;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InteresSimpleTest {

    private final InteresSimple calculo = new InteresSimple();

    @Test
    void cuotaRepartePorIgualCapitalMasLaTasaMensualPorElPlazo() {
        // 10000 x (1 + 0.03 x 24) = 17200 / 24
        assertThat(calculo.importeCuota(new BigDecimal("10000"), new BigDecimal("3"), 24))
            .isEqualByComparingTo("716.67");
    }

    @Test
    void cuotaRedondeaAlCentavoYLaSumaSePasaDosCentavos() {
        BigDecimal cuota = calculo.importeCuota(new BigDecimal("10000"), new BigDecimal("3"), 6);

        // 10000 x (1 + 0.03 x 6) = 11800 / 6
        assertThat(cuota).isEqualByComparingTo("1966.67");
        // 6 x 1966.67 = 11800.02: las cuotas cobran 2 centavos mas que capital + interes
        assertThat(cuota.multiply(BigDecimal.valueOf(6))).isEqualByComparingTo("11800.02");
    }

    @Test
    void tasaCeroDevuelveElCapitalYLaSumaQuedaUnCentavoAbajo() {
        BigDecimal capital = new BigDecimal("1000");

        BigDecimal cuota = calculo.importeCuota(capital, BigDecimal.ZERO, 3);
        assertThat(cuota).isEqualByComparingTo("333.33");
        assertThat(cuota.multiply(BigDecimal.valueOf(3))).isEqualByComparingTo("999.99");
    }

    @Test
    void tasaConDecimalesRedondeaLaTasaACuatroDecimales() {
        // 12.345% -> 0.12345 -> 0.1235 -> 1000 x 1.1235
        assertThat(calculo.importeCuota(new BigDecimal("1000"), new BigDecimal("12.345"), 1))
            .isEqualByComparingTo("1123.50");
    }
}
