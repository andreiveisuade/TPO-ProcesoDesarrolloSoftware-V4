package com.uade.tpejemplo.model.credito;

import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.model.cliente.Cliente;
import com.uade.tpejemplo.model.plan.TipoPlan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuotaTest {

    private Credito creditoOtorgado(LocalDate fecha) {
        Credito credito = Credito.nuevo(Cliente.nuevo("30111222", "Ana"), fecha,
            new CondicionesDeCredito(new BigDecimal("200"), BigDecimal.ZERO, 2, TipoPlan.INTERES_SIMPLE, Set.of()));
        return credito;
    }

    @Test
    void cuotaConVencimientoPasadoSinPagarEstaVencida() {
        Credito credito = creditoOtorgado(LocalDate.now().minusMonths(2));

        assertThat(credito.getCuotas().get(0).estaVencida()).isTrue();
        assertThat(credito.getCuotas().get(1).estaVencida()).isFalse();
    }

    @Test
    void venceElDiaSiguienteAlVencimiento() {
        Cuota cuota = creditoOtorgado(LocalDate.of(2026, 1, 10)).getCuotas().get(0);
        LocalDate vencimiento = cuota.getFechaVencimiento();

        assertThat(cuota.estaVencida(vencimiento)).isFalse();
        assertThat(cuota.estaVencida(vencimiento.plusDays(1))).isTrue();
    }

    @Test
    void cuotaDeUnCreditoAnuladoNoEstaVencida() {
        Credito credito = creditoOtorgado(LocalDate.now().minusMonths(2));
        credito.anular();

        assertThat(credito.getCuotas().get(0).estaVencida()).isFalse();
    }

    @Test
    void cuotaPagadaNoEstaVencida() {
        Cuota cuota = creditoOtorgado(LocalDate.now().minusMonths(2)).getCuotas().get(0);
        cuota.registrarCobranza(new BigDecimal("100.00"));

        assertThat(cuota.estaPagada()).isTrue();
        assertThat(cuota.estaVencida()).isFalse();
    }

    @Test
    void cobranzaAnuladaDejaLaCuotaImpaga() {
        Cuota cuota = creditoOtorgado(LocalDate.now()).getCuotas().get(0);
        cuota.registrarCobranza(new BigDecimal("100.00")).anular();

        assertThat(cuota.estaPagada()).isFalse();
    }

    @Test
    void rechazaCobrarDosVeces() {
        Cuota cuota = creditoOtorgado(LocalDate.now()).getCuotas().get(0);
        cuota.registrarCobranza(new BigDecimal("100.00"));

        assertThatThrownBy(() -> cuota.registrarCobranza(new BigDecimal("100.00")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("ya fue pagada");
    }

    @Test
    void rechazaCobrarSiElCreditoEstaAnulado() {
        Credito credito = creditoOtorgado(LocalDate.now());
        credito.anular();

        assertThatThrownBy(() -> credito.getCuotas().get(0).registrarCobranza(new BigDecimal("100.00")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("anulado");
    }

    @Test
    void rechazaImporteDistintoAlDeLaCuota() {
        Cuota cuota = creditoOtorgado(LocalDate.now()).getCuotas().get(0);

        assertThatThrownBy(() -> cuota.registrarCobranza(new BigDecimal("99.99")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("no coincide");
        assertThat(cuota.estaPagada()).isFalse();
    }

    @Test
    void aceptaElMismoImporteConOtraEscala() {
        Cuota cuota = creditoOtorgado(LocalDate.now()).getCuotas().get(0);
        cuota.registrarCobranza(new BigDecimal("100"));

        assertThat(cuota.estaPagada()).isTrue();
    }

    @Test
    void saldoEsElImporteHastaQueSePaga() {
        Cuota cuota = creditoOtorgado(LocalDate.now()).getCuotas().get(0);

        assertThat(cuota.saldo()).isEqualByComparingTo("100.00");
        cuota.registrarCobranza(new BigDecimal("100.00"));
        assertThat(cuota.saldo()).isEqualByComparingTo("0");
    }

    @Test
    void cuotaDeUnCreditoAnuladoNoTieneSaldoNiVencido() {
        Credito credito = creditoOtorgado(LocalDate.now().minusMonths(2));
        credito.anular();
        Cuota cuota = credito.getCuotas().get(0);

        assertThat(cuota.saldo()).isEqualByComparingTo("0");
        assertThat(cuota.montoVencido()).isEqualByComparingTo("0");
    }

    @Test
    void montoVencidoEsElImporteSoloSiEstaVencida() {
        Credito credito = creditoOtorgado(LocalDate.now().minusMonths(2));

        assertThat(credito.getCuotas().get(0).montoVencido()).isEqualByComparingTo("100.00");
        assertThat(credito.getCuotas().get(1).montoVencido()).isEqualByComparingTo("0");
    }
}
