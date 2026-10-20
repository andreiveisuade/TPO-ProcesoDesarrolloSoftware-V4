package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.request.CobranzaRequest;
import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.model.plan.TipoPlan;
import com.uade.tpejemplo.model.usuario.Permisos;
import com.uade.tpejemplo.model.usuario.Rol;
import com.uade.tpejemplo.model.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertFalse;

// I1: anular un credito y cobrar una cuota a la vez no deja un credito ANULADO con una cobranza vigente.
@SpringBootTest
class AnulacionConcurrenteTest {

    @Autowired ClienteService clienteService;
    @Autowired CreditoService creditoService;
    @Autowired CobranzaService cobranzaService;

    @Test
    void anularYCobrarALaVezNoDejanCobranzaVigenteEnCreditoAnulado() throws Exception {
        ClienteRequest cliente = new ClienteRequest();
        cliente.setDni("77000222");
        cliente.setNombre("Anulacion");
        clienteService.crear(cliente);
        Usuario supervisor = Usuario.nuevo("sup-concurrente", "x", Rol.SUPERVISOR, Permisos.todos());

        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 20; i++) {
            CreditoRequest credito = new CreditoRequest();
            credito.setDniCliente("77000222");
            credito.setDeudaOriginal(new BigDecimal("1000"));
            credito.setFecha(LocalDate.now());
            credito.setTasaInteres(BigDecimal.ZERO);
            credito.setCantidadCuotas(1);
            credito.setTipoPlan(TipoPlan.INTERES_SIMPLE);
            Long id = creditoService.crear(credito).getId();

            CountDownLatch salida = new CountDownLatch(1);
            Future<?> anular = pool.submit(() -> {
                salida.await();
                creditoService.anularCredito(id, supervisor);
                return null;
            });
            Future<?> cobrar = pool.submit(() -> {
                CobranzaRequest cobro = new CobranzaRequest();
                cobro.setIdCredito(id);
                cobro.setNumeroCuota(1);
                cobro.setImporte(new BigDecimal("1000"));
                salida.await();
                cobranzaService.registrar(cobro);
                return null;
            });
            salida.countDown();
            for (Future<?> f : new Future<?>[] {anular, cobrar}) {
                try {
                    f.get();
                } catch (Exception rechazado) {
                    // el que llega segundo se rechaza: el credito ya esta anulado o la cuota ya paga
                }
            }

            boolean anulado = creditoService.buscarPorId(id).isAnulado();
            boolean hayCobranzaVigente = cobranzaService.listarPorCredito(id).stream().anyMatch(c -> !c.isAnulada());
            assertFalse(anulado && hayCobranzaVigente, "credito ANULADO con cobranza vigente");
        }
        pool.shutdown();
    }
}
