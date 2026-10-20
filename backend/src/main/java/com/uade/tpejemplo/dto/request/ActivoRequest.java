package com.uade.tpejemplo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivoRequest {
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
