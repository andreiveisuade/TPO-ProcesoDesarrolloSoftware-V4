package com.uade.tpejemplo.dto.response;

import com.uade.tpejemplo.model.Rol;
import com.uade.tpejemplo.model.interfaces.IUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String username;
    private Rol rol;
    private boolean puedeAnularCredito;
    private boolean puedeAnularCobranza;
    private boolean activo;

    public static UsuarioResponse desde(IUsuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getRol(),
            usuario.puedeAnularCredito(),
            usuario.puedeAnularCobranza(),
            usuario.isActivo()
        );
    }
}
