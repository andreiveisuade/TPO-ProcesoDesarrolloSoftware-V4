package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.dto.response.CreditoResponse;
import com.uade.tpejemplo.model.usuario.IUsuario;

import java.util.List;

public interface CreditoService {

    CreditoResponse crear(CreditoRequest request);

    CreditoResponse buscarPorId(Long id);

    List<CreditoResponse> listarTodos();

    List<CreditoResponse> listarPorCliente(String dniCliente);

    void anularCredito(Long id, IUsuario usuario);
}
