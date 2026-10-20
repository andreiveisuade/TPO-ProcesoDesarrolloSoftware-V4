package com.uade.tpejemplo.model.interfaces;

import com.uade.tpejemplo.model.Rol;

public interface IUsuario {

    Long getId();

    String getUsername();

    String getPassword();

    Rol getRol();

    IPermisos getPermisos();

    boolean isActivo();

    boolean puedeAnularCredito();

    boolean puedeAnularCobranza();

    void otorgarPermisos(IPermisos permisos);

    void asignarRol(Rol rol);

    void desactivar();

    void reactivar();
}
