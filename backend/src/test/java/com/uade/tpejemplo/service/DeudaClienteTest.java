package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.dto.response.DeudaClienteResponse;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.plan.TipoPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// La deuda del cliente la calcula la cartera de sus creditos.
@SpringBootTest
class DeudaClienteTest {

    @Autowired ClienteService clienteService;
    @Autowired CreditoService creditoService;

    private void crearCliente(String dni) {
        ClienteRequest cliente = new ClienteRequest();
        cliente.setDni(dni);
        cliente.setNombre("Deuda");
        clienteService.crear(cliente);
    }

    private void crearCredito(String dni, LocalDate fecha) {
        CreditoRequest credito = new CreditoRequest();
        credito.setDniCliente(dni);
        credito.setDeudaOriginal(new BigDecimal("1000"));
        credito.setFecha(fecha);
        credito.setTasaInteres(BigDecimal.ZERO);
        credito.setCantidadCuotas(2);
        credito.setTipoPlan(TipoPlan.INTERES_SIMPLE);
        creditoService.crear(credito);
    }

    @Test
    void sumaSaldoYVencidoDeLosCreditosDelCliente() {
        crearCliente("77000555");
        crearCredito("77000555", LocalDate.now().minusMonths(1).minusDays(1));

        DeudaClienteResponse deuda = clienteService.deuda("77000555");

        assertThat(deuda.getSaldo()).isEqualByComparingTo("1000");
        assertThat(deuda.getMontoVencido()).isEqualByComparingTo("500");
    }

    @Test
    void clienteAlDiaNoTieneMontoVencido() {
        crearCliente("77000666");
        crearCredito("77000666", LocalDate.now());

        DeudaClienteResponse deuda = clienteService.deuda("77000666");

        assertThat(deuda.getSaldo()).isEqualByComparingTo("1000");
        assertThat(deuda.getMontoVencido()).isEqualByComparingTo("0");
    }

    @Test
    void clienteInexistenteDa404() {
        assertThatThrownBy(() -> clienteService.deuda("00000000"))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
