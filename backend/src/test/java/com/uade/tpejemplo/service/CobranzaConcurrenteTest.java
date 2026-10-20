package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.request.CobranzaRequest;
import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.model.plan.TipoPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

// I-1: 20 cobros simultaneos de la misma cuota dejan una sola cobranza vigente.
@SpringBootTest
class CobranzaConcurrenteTest {

    @Autowired ClienteService clienteService;
    @Autowired CreditoService creditoService;
    @Autowired CobranzaService cobranzaService;

    @Test
    void cobrosSimultaneosDeUnaCuotaDejanUnaSolaVigente() throws Exception {
        ClienteRequest cliente = new ClienteRequest();
        cliente.setDni("77000111");
        cliente.setNombre("Concurrente");
        clienteService.crear(cliente);

        CreditoRequest credito = new CreditoRequest();
        credito.setDniCliente("77000111");
        credito.setDeudaOriginal(new BigDecimal("1000"));
        credito.setFecha(LocalDate.now());
        credito.setTasaInteres(BigDecimal.ZERO);
        credito.setCantidadCuotas(1);
        credito.setTipoPlan(TipoPlan.INTERES_SIMPLE);
        Long idCredito = creditoService.crear(credito).getId();

        ExecutorService pool = Executors.newFixedThreadPool(20);
        List<Future<?>> cobros = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            cobros.add(pool.submit(() -> {
                CobranzaRequest cobro = new CobranzaRequest();
                cobro.setIdCredito(idCredito);
                cobro.setNumeroCuota(1);
                cobro.setImporte(new BigDecimal("1000"));
                try {
                    cobranzaService.registrar(cobro);
                } catch (RuntimeException rechazado) {
                    // los otros 19 cobros se rechazan: la cuota ya esta paga
                }
            }));
        }
        for (Future<?> cobro : cobros) cobro.get();
        pool.shutdown();

        long vigentes = cobranzaService.listarPorCredito(idCredito).stream()
            .filter(c -> !c.isAnulada()).count();
        assertEquals(1, vigentes);
    }
}
