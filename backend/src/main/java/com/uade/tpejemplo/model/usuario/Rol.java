package com.uade.tpejemplo.model.usuario;

// Rol de un usuario y su nombre como autoridad de Spring Security.
public enum Rol implements IRol {
    ADMIN,
    SUPERVISOR,
    USER;

    // Spring Security espera las autoridades con prefijo ROLE_. Es el rol
    // quien sabe como se llama para el framework, no quien lo consulta.
    @Override
    public String autoridad() {
        return "ROLE_" + name();
    }
}
