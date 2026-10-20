package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.LoginRequest;
import com.uade.tpejemplo.dto.request.RegisterRequest;
import com.uade.tpejemplo.dto.response.AuthResponse;
import com.uade.tpejemplo.dto.response.UsuarioResponse;
import com.uade.tpejemplo.model.usuario.Rol;

// Facade: interfaz simple (registrar, registrarConRol, login, actual) sobre Spring Security (AuthenticationManager, PasswordEncoder) y JWT (TokenService)
public interface AuthService {

    AuthResponse registrar(RegisterRequest request);

    UsuarioResponse registrarConRol(RegisterRequest request, Rol rol);

    AuthResponse login(LoginRequest request);

    AuthResponse actual(String username);
}
