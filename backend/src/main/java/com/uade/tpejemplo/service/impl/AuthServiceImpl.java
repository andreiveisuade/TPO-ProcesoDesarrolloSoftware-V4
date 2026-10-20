package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.LoginRequest;
import com.uade.tpejemplo.dto.request.RegisterRequest;
import com.uade.tpejemplo.dto.response.AuthResponse;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.Permisos;
import com.uade.tpejemplo.model.Rol;
import com.uade.tpejemplo.model.Usuario;
import com.uade.tpejemplo.repository.UsuarioRepository;
import com.uade.tpejemplo.service.TokenService;
import com.uade.tpejemplo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Login y registro: autentica contra Spring Security y emite el token.
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
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El usuario '" + request.getUsername() + "' ya existe");
        }

        Usuario usuario = Usuario.nuevo(
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            Rol.USER,
            Permisos.ninguno()
        );

        usuarioRepository.save(usuario);

        return AuthResponse.desde(tokenService.generarToken(usuario.getUsername()), usuario);
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

    private Usuario buscar(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("usuario", "username", username));
    }
}
