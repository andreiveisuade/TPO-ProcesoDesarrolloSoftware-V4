package com.uade.tpejemplo.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CarteraTest {

    private Credito creditoOtorgado(LocalDate fecha) {
        return Credito.nuevo(Cliente.nuevo("30111222", "Ana"), new BigDecimal("900"),
            fecha, BigDecimal.ZERO, 3, TipoPlan.INTERES_SIMPLE, Set.of());
    }

    @Test
    void carteraVaciaNoDebeNada() {
        Cartera cartera = Cartera.de(List.of());

        assertThat(cartera.saldo()).isEqualByComparingTo("0");
        assertThat(cartera.montoVencido()).isEqualByComparingTo("0");
    }

    @Test
    void elCreditoAnuladoNoSumaSaldo() {
        Credito vigente = creditoOtorgado(LocalDate.now());
        Credito anulado = creditoOtorgado(LocalDate.now());
        anulado.anular();

        assertThat(Cartera.de(List.of(vigente, anulado)).saldo()).isEqualByComparingTo("900.00");
    }

    @Test
    void sumaLasCuotasVencidasDeTodosLosCreditos() {
        Credito alDia = creditoOtorgado(LocalDate.now());
        Credito conDosVencidas = creditoOtorgado(LocalDate.now().minusMonths(2).minusDays(1));

        assertThat(Cartera.de(List.of(alDia, conDosVencidas)).montoVencido()).isEqualByComparingTo("600.00");
    }

    @Test
    void contestaLoMismoQueLasCuotasDeSusCreditos() {
        Credito credito = creditoOtorgado(LocalDate.now().minusMonths(2).minusDays(1));
        credito.getCuotas().get(0).registrarCobranza(new BigDecimal("300.00"));
        Cartera cartera = Cartera.de(List.of(credito));

        assertThat(cartera.saldo()).isEqualByComparingTo(Cartera.de(credito.getCuotas()).saldo());
        assertThat(cartera.saldo()).isEqualByComparingTo("600.00");
        assertThat(cartera.montoVencido()).isEqualByComparingTo("300.00");
    }
}
