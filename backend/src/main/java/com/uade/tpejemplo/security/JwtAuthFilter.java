package com.uade.tpejemplo.security;

import com.uade.tpejemplo.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

// Filtro que lee el token Bearer de cada request y autentica al usuario.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extraerToken(request);

        if (token != null && faltaAutenticar()) {
            autenticar(token, request);
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(PREFIJO_BEARER)) {
            return null;
        }
        return authHeader.substring(PREFIJO_BEARER.length());
    }

    private boolean faltaAutenticar() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    // extraerUsername ya valida firma y vencimiento. Un token invalido, vencido o
    // de un usuario que ya no existe o esta desactivado no es un error del filtro:
    // la request sigue sin autenticar y quien decide si eso alcanza es la cadena de seguridad.
    private void autenticar(String token, HttpServletRequest request) {
        Optional<String> username = tokenService.extraerUsername(token);
        if (username.isEmpty()) {
            return;
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username.get());
        } catch (UsernameNotFoundException e) {
            return;
        }
        if (!userDetails.isEnabled()) {
            return;
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
