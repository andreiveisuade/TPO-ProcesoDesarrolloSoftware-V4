package com.uade.tpejemplo.model.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Permisos de anulacion de un usuario. Viajan y se otorgan juntos:
// son un valor, no dos booleans sueltos.
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permisos implements IPermisos {

    @Column(name = "puede_anular_credito", nullable = false)
    private boolean puedeAnularCredito;

    @Column(name = "puede_anular_cobranza", nullable = false)
    private boolean puedeAnularCobranza;

    private Permisos(boolean puedeAnularCredito, boolean puedeAnularCobranza) {
        this.puedeAnularCredito = puedeAnularCredito;
        this.puedeAnularCobranza = puedeAnularCobranza;
    }

    public static Permisos de(boolean puedeAnularCredito, boolean puedeAnularCobranza) {
        return new Permisos(puedeAnularCredito, puedeAnularCobranza);
    }

    public static Permisos ninguno() {
        return new Permisos(false, false);
    }

    public static Permisos todos() {
        return new Permisos(true, true);
    }
}
