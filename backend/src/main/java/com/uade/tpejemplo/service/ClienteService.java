package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.response.ClienteResponse;
import com.uade.tpejemplo.dto.response.DeudaClienteResponse;

import java.util.List;

public interface ClienteService {

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse buscarPorDni(String dni);

    List<ClienteResponse> listarTodos();

    DeudaClienteResponse deuda(String dni);
}
