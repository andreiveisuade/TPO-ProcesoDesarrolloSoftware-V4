package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.model.TipoPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

// No se otorga un credito nuevo a un cliente con cuotas vencidas.
@SpringBootTest
class ReglaDeMoraTest {

    @Autowired ClienteService clienteService;
    @Autowired CreditoService creditoService;

    private void crearCliente(String dni) {
        ClienteRequest cliente = new ClienteRequest();
        cliente.setDni(dni);
        cliente.setNombre("Mora");
        clienteService.crear(cliente);
    }

    private CreditoRequest credito(String dni, LocalDate fecha) {
        CreditoRequest credito = new CreditoRequest();
        credito.setDniCliente(dni);
        credito.setDeudaOriginal(new BigDecimal("1000"));
        credito.setFecha(fecha);
        credito.setTasaInteres(BigDecimal.ZERO);
        credito.setCantidadCuotas(2);
        credito.setTipoPlan(TipoPlan.INTERES_SIMPLE);
        return credito;
    }

    @Test
    void rechazaCreditoNuevoSiElClienteTieneCuotasVencidas() {
        crearCliente("77000333");
        creditoService.crear(credito("77000333", LocalDate.now().minusMonths(2)));

        assertThatThrownBy(() -> creditoService.crear(credito("77000333", LocalDate.now())))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("tiene cuotas vencidas");
    }

    @Test
    void otorgaOtroCreditoSiElClienteEstaAlDia() {
        crearCliente("77000444");
        creditoService.crear(credito("77000444", LocalDate.now()));

        assertThatNoException().isThrownBy(() -> creditoService.crear(credito("77000444", LocalDate.now())));
    }
}
