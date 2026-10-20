package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.ActivoRequest;
import com.uade.tpejemplo.dto.request.AltaUsuarioRequest;
import com.uade.tpejemplo.dto.request.PermisosRequest;
import com.uade.tpejemplo.dto.request.RolRequest;
import com.uade.tpejemplo.dto.response.UsuarioResponse;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.Permisos;
import com.uade.tpejemplo.model.Rol;
import com.uade.tpejemplo.model.Usuario;
import com.uade.tpejemplo.repository.UsuarioRepository;
import com.uade.tpejemplo.service.AdminService;
import com.uade.tpejemplo.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Casos de uso de administracion de usuarios: listado, alta, rol, permisos y baja logica.
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getRol() != Rol.ADMIN)
                .map(UsuarioResponse::desde)
                .toList();
    }

    @Override
    public UsuarioResponse crearUsuario(AltaUsuarioRequest request) {
        // Facade: el alta del admin pasa por la misma interfaz que el registro
        return authService.registrarConRol(request, request.getRol());
    }

    @Transactional
    @Override
    public UsuarioResponse actualizarPermisos(Long id, PermisosRequest request) {
        Usuario usuario = buscar(id);

        usuario.otorgarPermisos(Permisos.de(
                request.getPuedeAnularCredito(),
                request.getPuedeAnularCobranza()
        ));

        usuarioRepository.save(usuario);

        return UsuarioResponse.desde(usuario);
    }

    @Transactional
    @Override
    public UsuarioResponse actualizarRol(Long id, RolRequest request) {
        Usuario usuario = buscar(id);

        usuario.asignarRol(request.getRol());
        usuarioRepository.save(usuario);

        return UsuarioResponse.desde(usuario);
    }

    @Transactional
    @Override
    public UsuarioResponse actualizarActivo(Long id, ActivoRequest request) {
        Usuario usuario = buscar(id);

        if (request.getActivo()) {
            usuario.reactivar();
        } else {
            usuario.desactivar();
        }
        usuarioRepository.save(usuario);

        return UsuarioResponse.desde(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("usuario", "id", id));
    }
}
