package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.ActivoRequest;
import com.uade.tpejemplo.dto.request.AltaUsuarioRequest;
import com.uade.tpejemplo.dto.request.PermisosRequest;
import com.uade.tpejemplo.dto.request.RolRequest;
import com.uade.tpejemplo.dto.response.UsuarioResponse;

import java.util.List;

public interface AdminService {

    List<UsuarioResponse> listarUsuarios();

    UsuarioResponse crearUsuario(AltaUsuarioRequest request);

    UsuarioResponse actualizarPermisos(Long id, PermisosRequest request);

    UsuarioResponse actualizarRol(Long id, RolRequest request);

    UsuarioResponse actualizarActivo(Long id, ActivoRequest request);
}
