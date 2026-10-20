package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.response.ClienteResponse;
import com.uade.tpejemplo.dto.response.DeudaClienteResponse;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.Cartera;
import com.uade.tpejemplo.model.Cliente;
import com.uade.tpejemplo.model.interfaces.Deuda;
import com.uade.tpejemplo.repository.ClienteRepository;
import com.uade.tpejemplo.repository.CreditoRepository;
import com.uade.tpejemplo.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Casos de uso de clientes: alta, busqueda, listado y deuda.
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CreditoRepository creditoRepository;

    @Transactional
    @Override
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsById(request.getDni())) {
            throw new BusinessException("Ya existe un cliente con DNI: " + request.getDni());
        }
        Cliente cliente = Cliente.nuevo(request.getDni(), request.getNombre());
        clienteRepository.save(cliente);
        return ClienteResponse.desde(cliente);
    }

    @Transactional(readOnly = true)
    @Override
    public ClienteResponse buscarPorDni(String dni) {
        Cliente cliente = clienteRepository.findById(dni)
            .orElseThrow(() -> new ResourceNotFoundException("cliente", "DNI", dni));
        return ClienteResponse.desde(cliente);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
            .map(ClienteResponse::desde)
            .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public DeudaClienteResponse deuda(String dni) {
        if (!clienteRepository.existsById(dni)) {
            throw new ResourceNotFoundException("cliente", "DNI", dni);
        }
        // Composite: la cartera suma la deuda de los creditos del cliente sin que el servicio recorra cuotas
        Deuda cartera = Cartera.de(creditoRepository.buscarPorCliente(dni));
        return new DeudaClienteResponse(dni, cartera.saldo(), cartera.montoVencido());
    }
}
