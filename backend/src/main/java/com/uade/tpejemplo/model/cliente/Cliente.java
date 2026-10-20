package com.uade.tpejemplo.model.cliente;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Cliente de la financiera, identificado por su DNI.
@Entity
@Table(name = "clientes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cliente implements ICliente {

    @Id
    @Column(name = "dni", length = 15)
    private String dni;

    @NotBlank
    @Column(name = "nombre", nullable = false)
    private String nombre;

    private Cliente(String dni, String nombre) {
        this.dni = dni;
        this.nombre = nombre;
    }

    public static Cliente nuevo(String dni, String nombre) {
        return new Cliente(dni, nombre);
    }
}
