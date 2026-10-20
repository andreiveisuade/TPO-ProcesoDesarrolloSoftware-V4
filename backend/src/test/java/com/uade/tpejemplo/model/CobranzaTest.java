package com.uade.tpejemplo.model;

import com.uade.tpejemplo.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CobranzaTest {

    @Test
    void seRegistraConFechaDeHoyYSePuedeAnularElMismoDia() {
        Credito credito = Credito.nuevo(Cliente.nuevo("30111222", "Ana"), new BigDecimal("100"),
            LocalDate.now(), BigDecimal.ZERO, 1, TipoPlan.INTERES_SIMPLE, Set.of());
        Cobranza cobranza = credito.getCuotas().get(0).registrarCobranza(new BigDecimal("100.00"));

        assertThat(cobranza.getFechaCobranza()).isEqualTo(LocalDate.now());
        cobranza.anular();
        assertThat(cobranza.isAnulada()).isTrue();
    }

    @Test
    void noSePuedeAnularDosVeces() {
        Credito credito = Credito.nuevo(Cliente.nuevo("30111222", "Ana"), new BigDecimal("100"),
            LocalDate.now(), BigDecimal.ZERO, 1, TipoPlan.INTERES_SIMPLE, Set.of());
        Cobranza cobranza = credito.getCuotas().get(0).registrarCobranza(new BigDecimal("100.00"));
        cobranza.anular();

        assertThatThrownBy(cobranza::anular).isInstanceOf(BusinessException.class);
    }

    @Test
    void noSePuedeAnularUnaCobranzaDeOtroDia() {
        Credito credito = Credito.nuevo(Cliente.nuevo("30111222", "Ana"), new BigDecimal("100"),
            LocalDate.now(), BigDecimal.ZERO, 1, TipoPlan.INTERES_SIMPLE, Set.of());
        Cobranza cobranza = credito.getCuotas().get(0).registrarCobranza(new BigDecimal("100.00"));

        assertThatThrownBy(() -> cobranza.anular(LocalDate.now().plusDays(1)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("día de hoy");
        assertThat(cobranza.isAnulada()).isFalse();
    }
}
