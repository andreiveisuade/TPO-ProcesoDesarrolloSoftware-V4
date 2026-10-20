# Backlog consolidado de mejoras pendientes

Junta lo que quedó abierto en las iteraciones 3 y 4 (dominio, prácticas de Spring, bad smells, tests, API y front), contrastado con el código de `main`. No se lista lo que ya está hecho (V4: Composite M11, Decorator M12, Facade M13, regla de mora M14, deuda del cliente M15, cargos en el panel M16, BS14; V3: O1-O7, S5, BS2, I-2, T-1, T-5, H4 con `GET /api/auth/me`, pantallas de UC06 y UC09, S1-S4, D1, D2, BS1, DF1, DF3, BSF1, título ADMIN, `@Valid` de permisos y rol, 401 sin token, `GET /api/creditos`, `tipoPlan` en `CreditoResponse`, `esValido` fuera de `TokenService` (BS12), I7 (precarga por efecto lateral eliminada: lazy loading dentro de la transacción, BS13), I-1 (bloqueo pesimista en `…/repository/CuotaRepository.java` `buscarPorCreditoYNumero`, test en `backend/src/test/java/com/uade/tpejemplo/service/CobranzaConcurrenteTest.java`), I1 (bloqueo del crédito al anular y cobrar, `9c3d4ae`), S7-S9 y TPO-008 (perfiles dev y prod, CORS con `CORS_ORIGIN`, consola H2 solo en dev), BS3, BS4, BS6, TPO-012, 403 con token y sin rol, 400 en requests mal formadas, mejoras de UI, dark theme, comentarios y limpieza del repo).

`…/` = `backend/src/main/java/com/uade/tpejemplo/`. Rutas de front relativas a `frontend/src/`. Tamaño: S (pocas líneas), M (varios archivos), L (cambia el modelo). Tamaño con `*` = estimación. Fechas: 27/10 y 03/11 son iteraciones, 17/11 es la entrega final.

## Dominio y patrones

| ID | Qué | Ruta | Tam. | Prioridad | Motivo |
|---|---|---|---|---|---|
| H8 | Trazabilidad de usuario en `Cobranza`: quién registró y quién anuló | `…/model/Cobranza.java`, `…/controller/CobranzaController.java`, `…/dto/response/CobranzaResponse.java` | M* | 03/11 | Modelo nuevo que toca la API. Sigue pendiente después de V4; no es un Decorator de auditoría (la exploración de V4 lo descartó) |
| O3b | Ajustar la última cuota para que el total cierre exacto (hoy 10.000 al 45 % en 6 cuotas: total 14.500,02) | `…/model/plan/InteresSimple.java`, `…/model/plan/SistemaFrances.java`, `…/model/Credito.java` | M | 03/11 | Alternativa que O3 dejó para después; hoy el centavo se explica como redondeo por cuota |
| O8 | Pagos parciales (A-01) | `…/model/Cuota.java`, `…/model/Cobranza.java`, `…/model/Credito.java` | L | — | Cambia `Cuota.estaPagada()`, `Cobranza`, `Credito.saldo()`, el dashboard y la vista. Exigir el importe exacto (TPO-006) se defiende para este alcance; queda fuera de V3 |
| P-M9 | Tasa con unidad declarada por plan; cuota con capital/interés separados | `…/model/TipoPlan.java`, `…/model/Cuota.java` | M* | — | Límites del Strategy actual (ver reporte V3, M9). Sin el desglose de interés tampoco se puede agregar IVA sobre intereses como decorador (M12) |
| M12b | Seguro de vida sobre el saldo deudor | `…/model/plan/SeguroDeVida.java` | M* | — | Simplificación de M12: hoy es 0,1 % del capital por cuota |

## Spring

| ID | Qué | Ruta | Tam. | Prioridad | Motivo |
|---|---|---|---|---|---|
| DB-1 | Base persistente para prod: PostgreSQL con migraciones (Flyway) y alta del primer admin | `backend/src/main/resources/application-prod.properties`, `backend/pom.xml` | M* | 17/11 | Prod sigue con H2 en memoria: pierde todo al reiniciar y, sin `DataInitializer`, no tiene usuarios |
| S3b | `@ConfigurationProperties` para las propiedades de JWT | `…/security/JwtUtil.java`, `backend/src/main/resources/application.properties` | S* | 03/11 | Propiedades de JWT tipadas en vez de leer `jwt.secret` suelto en `JwtUtil` |
| S10 | Renombrar el paquete `com.uade.tpejemplo`, `TpEjemploApplication` y el `artifactId` (TPO-014) | `backend/pom.xml`, todo `backend/src` | M | 17/11 | El nombre de plantilla no comunica el dominio. Diff ruidoso que taparía los cambios reales: NO antes del 13/10, evaluar para la entrega final |

## Tests

| ID | Qué | Ruta | Tam. | Prioridad | Motivo |
|---|---|---|---|---|---|
| T-2 | `@DataJpaTest` de las queries del dashboard (M3) sobre H2 con créditos, cuotas vencidas y cobranzas anuladas | `backend/src/test/java/com/uade/tpejemplo/repository/` | M* | 27/10 | Repositories y queries sin cobertura |
| T-3 | Tests de `Permisos` y `IPermisos` | `backend/src/test/java/com/uade/tpejemplo/model/` | S* | 27/10 | `Permisos` no tiene tests |

## API

| ID | Qué | Ruta | Tam. | Prioridad | Motivo |
|---|---|---|---|---|---|
| A-2 | Quitar `PUT /api/admin/usuarios/{id}/permisos` o justificarlo | `…/controller/AdminController.java` | S | 03/11 | Duplicado del de supervisor y sin uso en el front |
| A-3 | `GET /api/cobranzas/credito/{id}` con crédito inexistente responde 404 en vez de `[]` | `…/controller/CobranzaController.java`, `…/service/impl/CobranzaServiceImpl.java` | S* | 03/11 | Observación del mapa de API |

## Descartados (NO HACER)

| ID | Qué | Motivo |
|---|---|---|
| C1 | Renombrar `CalculoDeCuota` a `ICalculoDeCuota` | Decidido: queda sin prefijo `I` por ser el nombre de rol de la interfaz Strategy (ver convenciones en `docs/entrega/lista-mejoras.md`) |
| O9 | `EstadoCredito` como patrón State | El estado es derivado, no se guarda y hay una sola transición con regla (`Credito.anular()`). Sobreingeniería; el apunte advierte contra confundir Strategy con State. Respuesta para la defensa |
| O10 | Strategy para punitorios, Adapter sobre `PasswordEncoder`, Adapter sobre `fetch` | Ninguno tapa un hueco: no hay punitorios, `PasswordEncoder` ya es la abstracción de Spring y `api/apiClient.js` ya hace de pasarela |
| S6 | Una sola fuente de autorización por rol (sacar `@PreAuthorize` o las reglas de URL) | Decisión: se mantienen las dos. Las reglas de URL de `…/config/SecurityConfig.java` cubren las tres zonas y `@PreAuthorize` en `AdminController` y `SupervisorController` repite el control en el método: defensa en profundidad, si alguien cambia una ruta el método sigue protegido |
| D3 | Borrar `estaCancelado()`, `tieneCobranzas()` y `puedeAnularse()` de `…/model/interfaces/ICredito.java` | Tira tests y una pregunta de dominio legítima; no molesta a nadie |
| D4 | Borrar `…/model/interfaces/IRol.java` | Las interfaces de modelo se restituyeron en V2 a propósito; sacar una sola rompe la simetría del diagrama |
| D5 | Sacar `GET /api/creditos` por falta de consumidor | Es API pública documentada, no código muerto |
| BS5 | Objeto parámetro para `Credito.nuevo` (6 parámetros) | Datos obligatorios de tipos distintos y un solo llamador: sobreingeniería |
| BS7 | Quitar `@Data` de los DTO | Un DTO es una estructura de datos por diseño; las clases de dominio ya tienen comportamiento |
| BSF3 | Factorizar thunks y estilos inline del front | Boilerplate estándar de Redux Toolkit; cambia todo el front sin tapar ningún hueco |
| A-4 | `PUT /api/clientes/{dni}` (editar nombre) | DUDOSA: ningún caso de uso ni ticket lo pide |
| A-5 | `POST /api/creditos/{id}/anulacion` y `POST /api/cobranzas/{id}/anulacion` en vez de `DELETE` | DUDOSA: más REST pero cosmético, cambia el contrato y toca `api/creditos.js` y `api/cobranzas.js` |
| A-6 | `GET /api/cobranzas/{id}` | DUDOSA: nadie la consume |
| A-7 | Desactivar o borrar usuarios | DUDOSA: no hay caso de uso ni campo `activo`; requiere cambio de modelo |
| A-8 | `DELETE /api/clientes/{dni}`, `PUT` o `DELETE` real de crédito y de cobranza, CRUD de cuota, `GET /api/creditos/{id}/cuotas` | Romperían el historial o las cuotas ya generadas, o son redundantes con `CreditoResponse.cuotas` |

## Del code review V2 → V3 (06/10)

Pendientes del code review (`docs/trabajo/code-review-v2-v3.md`, en GitHub): **A3** (alta de cliente o usuario con el mismo DNI o username en simultáneo; resolver con unique + manejo de la violación), **5 medias** y **4 bajas**. A1 y A2 ya están resueltos.
