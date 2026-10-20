package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.response.DashboardStatsResponse;
import com.uade.tpejemplo.model.Cartera;
import com.uade.tpejemplo.model.Credito;
import com.uade.tpejemplo.model.EstadoCredito;
import com.uade.tpejemplo.repository.ClienteRepository;
import com.uade.tpejemplo.repository.CreditoRepository;
import com.uade.tpejemplo.repository.CobranzaRepository;
import com.uade.tpejemplo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// Arma las estadisticas del dashboard a partir de los creditos y las cobranzas.
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClienteRepository clienteRepository;
    private final CreditoRepository creditoRepository;
    private final CobranzaRepository cobranzaRepository;

    @Transactional(readOnly = true)
    @Override
    public DashboardStatsResponse obtenerEstadisticasGenerales() {
        long clientes = clienteRepository.count();
        List<Credito> creditos = creditoRepository.findAll();
        long creditosVigentes = creditos.stream().filter(credito -> credito.estado() == EstadoCredito.VIGENTE).count();

        BigDecimal totalFinanciado = creditoRepository.sumarDeudaOriginalNoAnulada();
        BigDecimal totalCobrado = cobranzaRepository.sumarImporteNoAnulado();

        // Composite: la cartera suma lo que cada credito suma de sus cuotas
        Cartera cartera = Cartera.de(creditos);
        BigDecimal saldoPendiente = cartera.saldo();
        BigDecimal montoVencido = cartera.montoVencido();

        return new DashboardStatsResponse(clientes, creditosVigentes, totalFinanciado, totalCobrado,
            saldoPendiente, montoVencido);
    }
}
