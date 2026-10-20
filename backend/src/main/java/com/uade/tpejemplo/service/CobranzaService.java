package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.CobranzaRequest;
import com.uade.tpejemplo.dto.response.CobranzaResponse;
import com.uade.tpejemplo.model.usuario.IUsuario;

import java.util.List;

public interface CobranzaService {

    CobranzaResponse registrar(CobranzaRequest request);

    List<CobranzaResponse> listarPorCredito(Long idCredito);

    void anularCobranza(Long id, IUsuario usuario);
}
