 package com.uade.tpejemplo.dto.request;

import com.uade.tpejemplo.model.usuario.Rol;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RolRequest {
    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
} 