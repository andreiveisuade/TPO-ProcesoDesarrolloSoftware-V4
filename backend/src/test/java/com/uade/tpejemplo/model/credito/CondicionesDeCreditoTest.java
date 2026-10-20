package com.uade.tpejemplo.model.credito;

import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.model.plan.TipoPlan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CondicionesDeCreditoTest {

    @Test
    void rechazaCondicionesInvalidas() {
        assertThatThrownBy(() -> new CondicionesDeCredito(BigDecimal.ZERO, BigDecimal.ONE, 3, TipoPlan.INTERES_SIMPLE, Set.of()))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> new CondicionesDeCredito(BigDecimal.TEN, BigDecimal.ONE, 0, TipoPlan.INTERES_SIMPLE, Set.of()))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> new CondicionesDeCredito(BigDecimal.TEN, new BigDecimal("-1"), 3, TipoPlan.INTERES_SIMPLE, Set.of()))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> new CondicionesDeCredito(BigDecimal.TEN, BigDecimal.ONE, 3, null, Set.of()))
            .isInstanceOf(BusinessException.class);
    }
}
