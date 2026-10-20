package com.uade.tpejemplo.security;

import com.uade.tpejemplo.model.interfaces.IUsuario;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Expone el Usuario del dominio como UserDetails; implementa solo lo que el
// dominio sabe contestar y el resto queda con el default de la interfaz.
// Adapter: adapta IUsuario a UserDetails de Spring Security
@Getter
@RequiredArgsConstructor
public class UsuarioDetails implements UserDetails {

    private final IUsuario usuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(usuario.getRol().autoridad()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    // Traduce el concepto del dominio (baja logica) al contrato de Spring.
    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }
}
