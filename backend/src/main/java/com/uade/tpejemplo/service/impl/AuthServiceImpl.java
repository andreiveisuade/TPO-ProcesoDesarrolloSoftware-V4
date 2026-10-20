package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.LoginRequest;
import com.uade.tpejemplo.dto.request.RegisterRequest;
import com.uade.tpejemplo.dto.response.AuthResponse;
import com.uade.tpejemplo.dto.response.UsuarioResponse;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.usuario.Permisos;
import com.uade.tpejemplo.model.usuario.Rol;
import com.uade.tpejemplo.model.usuario.Usuario;
import com.uade.tpejemplo.repository.UsuarioRepository;
import com.uade.tpejemplo.service.AuthService;
import com.uade.tpejemplo.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Login y registro (propio o por el ADMIN): autentica contra Spring Security y emite el token.
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Transactional
    @Override
    public AuthResponse registrar(RegisterRequest request) {
        Usuario usuario = crear(request, Rol.USER);

        return AuthResponse.desde(tokenService.generarToken(usuario.getUsername()), usuario);
    }

    // Sin token: lo usa el ADMIN para dar de alta a otro, no para entrar.
    @Transactional
    @Override
    public UsuarioResponse registrarConRol(RegisterRequest request, Rol rol) {
        if (rol == Rol.ADMIN) {
            throw new BusinessException("No se puede dar de alta un administrador");
        }
        return UsuarioResponse.desde(crear(request, rol));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Usuario usuario = buscar(request.getUsername());

        return AuthResponse.desde(tokenService.generarToken(usuario.getUsername()), usuario);
    }

    // Sin token nuevo: el front conserva el que ya tiene y solo refresca rol y permisos.
    @Override
    public AuthResponse actual(String username) {
        return AuthResponse.desde(null, buscar(username));
    }

    private Usuario crear(RegisterRequest request, Rol rol) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El usuario '" + request.getUsername() + "' ya existe");
        }

        Usuario usuario = Usuario.nuevo(
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            rol,
            Permisos.ninguno()
        );

        return usuarioRepository.save(usuario);
    }

    private Usuario buscar(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("usuario", "username", username));
    }
}
