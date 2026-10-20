# Demo en nvim

`docs/demo.sh` abre nvim con una tab por archivo cambiado: v2 a la izquierda (solo lectura), main a la derecha, en modo diff. El título de cada tab es el ID del cambio y el archivo. Moverse con `gt` / `gT`, salir con `:qa`.

`docs/demo.sh --headless` solo verifica que la sesión se arma sin errores. Los IDs son los del reporte de V3 (`docs/reporte/reporte-v3.html` en el tag `v3`).

## Orden y qué decir

1. **M9 Strategy** (`Credito`, `CalculoDeCuota`, `plan/*`, `TipoPlan`): `Credito` ya no conoce ninguna fórmula, delega en la estrategia de su `TipoPlan`. El francés entra como segunda implementación real y revierte la decisión de V2.
2. **M7 Adapter** (`TokenService`, `JwtUtil`, `JwtAuthFilter`, `UsuarioDetails`): la dependencia apunta a una abstracción propia y jjwt queda encerrada en un solo Adapter. `UsuarioDetails` aísla el dominio de Spring Security.
3. **M8 + O6 EstadoCredito, agregado y Creator** (`EstadoCredito`, `Credito`, `Cuota`, `CreditoServiceImpl`): el estado deja de ser boolean y el crédito sabe su saldo. `Credito` crea sus cuotas al otorgarse (Creator + cascade) y el service ya no las arma. Con cobranzas no se anula: lo decide `Cuota.estaPagada()`.
4. **M4 + BS3 permisos en el modelo** (`CreditoController`, `Usuario`, `CreditoServiceImpl`): el controller identifica al usuario y el modelo decide con `Usuario.puedeAnularCredito()`. La vista solo oculta, la regla se valida en el backend.
5. **M5 errores, seguridad y perfiles** (`GlobalExceptionHandler`, `SecurityConfig`, `OpenApiConfig`, `application-dev/prod`): 401 sin token, 403 sin rol o permiso, 400 por body mal formado o regla de negocio; el 500 no filtra el mensaje interno. Perfil dev con consola H2, prod sin ella y con CORS por variable.
6. **M3 + O7 Dashboard** (`DashboardServiceImpl`, `DashboardStatsResponse`): los números excluyen lo anulado (activos = VIGENTE); saldo pendiente y monto vencido salen del `Credito` y de la `Cuota`, que son los que tienen los datos.
7. **Cortos**:
   - **M1** (`Cuota`): no se cobra una cuota de un crédito anulado, la regla vive en `Cuota`.
   - **M6** (`Cuota`): `estaVencida()` en la entidad que tiene los datos.
   - **M10** (`SecurityConfig`): el ADMIN ve el dashboard; M10 no toca permisos.
   - **I-1** (`CuotaRepository`, `CobranzaServiceImpl`): bloqueo pesimista al buscar la cuota, dos cobros simultáneos de la misma cuota se serializan y el segundo falla.

## Mostrar la app en vivo

```bash
cd backend && mvn spring-boot:run          # http://localhost:8080, perfil dev
cd frontend && npm install && npm run dev  # http://localhost:5173
```

- **Usuarios** (password = usuario): `admin` (roles y permisos), `supervisor` (dashboard y todas las anulaciones), `user` (sin permisos de anulación).
- **Swagger**: <http://localhost:8080/swagger-ui.html>. Login en `/api/auth/login`, copiar el token y pegarlo en Authorize. Sirve para mostrar 401 sin token y 403 con `user` anulando.
- **Consola H2**: <http://localhost:8080/h2-console>, JDBC URL `jdbc:h2:mem:tpdb`, usuario `sa`, sin password. Mostrar la columna `tipo_plan` y las cuotas generadas de un crédito.
