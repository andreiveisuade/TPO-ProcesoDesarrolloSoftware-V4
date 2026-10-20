package com.uade.tpejemplo.dto.response;

import com.uade.tpejemplo.model.cliente.ICliente;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClienteResponse {

    private String dni;
    private String nombre;

    public static ClienteResponse desde(ICliente cliente) {
        return new ClienteResponse(cliente.getDni(), cliente.getNombre());
    }
}
