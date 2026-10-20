package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.LoginRequest;
import com.uade.tpejemplo.dto.request.RegisterRequest;
import com.uade.tpejemplo.dto.response.AuthResponse;

// Facade: interfaz simple (registrar, login, actual) sobre Spring Security (AuthenticationManager, PasswordEncoder) y JWT (TokenService)
public interface AuthService {

    AuthResponse registrar(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse actual(String username);
}
