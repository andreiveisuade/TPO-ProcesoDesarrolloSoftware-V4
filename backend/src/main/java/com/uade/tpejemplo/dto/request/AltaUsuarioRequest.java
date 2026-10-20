package com.uade.tpejemplo.dto.request;

import com.uade.tpejemplo.model.usuario.Rol;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Alta hecha por el ADMIN: los datos del registro mas el rol elegido.
@Getter
@Setter
public class AltaUsuarioRequest extends RegisterRequest {

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}
