package com.uade.tpejemplo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.dto.response.CreditoResponse;
import com.uade.tpejemplo.model.plan.TipoCargo;
import com.uade.tpejemplo.model.plan.TipoPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Un credito leido de la base se serializa con sus cargos fuera de la transaccion.
@SpringBootTest
class CreditoConCargosTest {

    @Autowired ClienteService clienteService;
    @Autowired CreditoService creditoService;
    @Autowired ObjectMapper objectMapper;

    @Test
    void elCreditoLeidoDeLaBaseSeSerializaConSusCargos() throws Exception {
        ClienteRequest cliente = new ClienteRequest();
        cliente.setDni("77000777");
        cliente.setNombre("Cargos");
        clienteService.crear(cliente);

        CreditoRequest request = new CreditoRequest();
        request.setDniCliente("77000777");
        request.setDeudaOriginal(new BigDecimal("1000"));
        request.setFecha(LocalDate.now());
        request.setTasaInteres(BigDecimal.ZERO);
        request.setCantidadCuotas(2);
        request.setTipoPlan(TipoPlan.INTERES_SIMPLE);
        request.setCargos(Set.of(TipoCargo.SEGURO_DE_VIDA));
        Long id = creditoService.crear(request).getId();

        CreditoResponse leido = creditoService.buscarPorId(id);

        assertThat(objectMapper.writeValueAsString(leido)).contains("SEGURO_DE_VIDA");
    }
}
