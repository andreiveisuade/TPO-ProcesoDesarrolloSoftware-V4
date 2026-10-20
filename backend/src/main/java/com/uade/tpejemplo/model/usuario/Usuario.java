package com.uade.tpejemplo.model.usuario;

import com.uade.tpejemplo.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
// Usuario del sistema con su rol y sus permisos de anulacion.
@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario implements IUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Embedded
    private Permisos permisos;

    // Baja logica: el usuario inactivo no entra, pero su historial queda.
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activo = true;

    private Usuario(String username, String passwordHasheado, Rol rol, Permisos permisos) {
        this.username = username;
        this.password = passwordHasheado;
        this.rol = rol;
        this.permisos = permisos;
    }

    // Unica forma de dar de alta un usuario: rol y permisos explicitos; el id lo asigna la base.
    public static Usuario nuevo(String username, String passwordHasheado, Rol rol, Permisos permisos) {
        return new Usuario(username, passwordHasheado, rol, permisos);
    }

    public boolean puedeAnularCredito() {
        return permisos.isPuedeAnularCredito();
    }

    public boolean puedeAnularCobranza() {
        return permisos.isPuedeAnularCobranza();
    }

    // Recibe la interfaz y guarda una copia en Permisos, que es lo que JPA persiste embebido.
    public void otorgarPermisos(IPermisos permisos) {
        if (rol == Rol.ADMIN) {
            throw new BusinessException("No se pueden modificar los permisos de un administrador");
        }
        this.permisos = Permisos.de(permisos.isPuedeAnularCredito(), permisos.isPuedeAnularCobranza());
    }

    // El rol se cambia con nombre de negocio en vez de un setter suelto:
    // quien lo llama esta asignando un rol, no escribiendo un campo.
    public void asignarRol(Rol rol) {
        if (this.rol == Rol.ADMIN) {
            throw new BusinessException("No se puede cambiar el rol de un administrador");
        }
        if (rol == Rol.ADMIN) {
            throw new BusinessException("No se puede otorgar el rol de administrador");
        }
        this.rol = rol;
    }

    public void desactivar() {
        if (rol == Rol.ADMIN) {
            throw new BusinessException("No se puede desactivar un administrador");
        }
        this.activo = false;
    }

    public void reactivar() {
        this.activo = true;
    }
}
