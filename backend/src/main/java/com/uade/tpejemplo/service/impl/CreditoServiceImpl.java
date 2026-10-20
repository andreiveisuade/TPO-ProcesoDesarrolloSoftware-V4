package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.dto.response.CreditoResponse;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.Cliente;
import com.uade.tpejemplo.model.Credito;
import com.uade.tpejemplo.repository.ClienteRepository;
import com.uade.tpejemplo.repository.CreditoRepository;
import com.uade.tpejemplo.service.CreditoService;
import com.uade.tpejemplo.model.interfaces.IUsuario;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

// Casos de uso de creditos: otorgar, consultar y anular. Las reglas las decide el modelo.
@Service
@RequiredArgsConstructor
public class CreditoServiceImpl implements CreditoService {

    private final CreditoRepository creditoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    @Override
    public CreditoResponse crear(CreditoRequest request) {
        Cliente cliente = buscarCliente(request.getDniCliente());

        Credito credito = creditoRepository.save(Credito.nuevo(
            cliente,
            request.getDeudaOriginal(),
            request.getFecha(),
            request.getTasaInteres(),
            request.getCantidadCuotas(),
            request.getTipoPlan(),
            request.getCargos() == null ? Set.of() : request.getCargos()
        ));

        return CreditoResponse.desde(credito);
    }

    @Transactional(readOnly = true)
    @Override
    public CreditoResponse buscarPorId(Long id) {
        return CreditoResponse.desde(buscarCredito(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CreditoResponse> listarTodos() {
        return creditoRepository.findAll().stream()
            .map(CreditoResponse::desde)
            .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CreditoResponse> listarPorCliente(String dniCliente) {
        if (!clienteRepository.existsById(dniCliente)) {
            throw new ResourceNotFoundException("cliente", "DNI", dniCliente);
        }
        return creditoRepository.buscarPorCliente(dniCliente).stream()
            .map(CreditoResponse::desde)
            .toList();
    }

    @Transactional
    @Override
    public void anularCredito(Long id, IUsuario usuario) {
        if (!usuario.puedeAnularCredito()) {
            throw new AccessDeniedException("El usuario no tiene permiso para anular creditos");
        }

        Credito credito = creditoRepository.bloquearPorId(id)
            .orElseThrow(() -> new ResourceNotFoundException("crédito", "id", id));

        credito.anular();
        creditoRepository.save(credito);
    }

    private Cliente buscarCliente(String dni) {
        return clienteRepository.findById(dni)
            .orElseThrow(() -> new ResourceNotFoundException("cliente", "DNI", dni));
    }

    private Credito buscarCredito(Long id) {
        return creditoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("crédito", "id", id));
    }
}
