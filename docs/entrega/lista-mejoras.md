# Lista de mejoras — Iteraciones 3 y 4 (TPO Grupo 7, Dashboard de préstamos)

Rutas Java relativas a `backend/src/main/java/com/uade/tpejemplo/`. Reporte completo: `docs/reporte/reporte-v4.html` (el de V3 está en el tag `v3`). Casos de uso: `docs/casos-de-uso/README.md`. Diagrama de clases: `docs/diagramas/clases-v4-mvc.svg` (capas MVC), `clases-v4-seguridad.svg` (seguridad, Facade y Adapter) y `clases-v4-modelo.svg` (modelo, Composite, Strategy y Decorator) (fuente `clases-v4.puml`). DER: `docs/diagramas/der-v4.svg`.

## Iteración 4: Composite, Decorator y Facade

| ID | Qué | Clase y método | Patrón / concepto |
|---|---|---|---|
| M11 | Saldo y monto vencido con la misma interfaz en cuota, crédito y cartera. La cuota responde (hoja); el crédito y la cartera suman a sus hijos. El `if (anulado)` de `Credito.saldo` pasa a la cuota y `sumarCuotas(Predicate)` pasa a `sumar(Function)` | `model/interfaces/Deuda.java`; `model/Cuota.java` `saldo`, `montoVencido`; `model/Credito.java` `saldo`, `montoVencido`, `sumar`; `model/Cartera.java` `de`, `saldo`, `montoVencido`; `service/impl/DashboardServiceImpl.java` `obtenerEstadisticasGenerales` | **Composite** (+ Polymorphism, OCP) |
| M12 | Cargos opcionales sobre la cuota: seguro de vida (0,1 % del capital por cuota; simplificación: el real va sobre el saldo) y gasto de administración ($500 por cuota). Cada cargo envuelve el cálculo de la Strategy y le suma lo suyo; se otorgan o no por crédito. Expansión del dominio, no corrección de un bug | `model/plan/CargoSobreCuota.java` `importeCuota`; `model/plan/SeguroDeVida.java`, `model/plan/GastoDeAdministracion.java` `cargo`; `model/TipoCargo.java` `aplicarSobre`; `model/Credito.java` `calculo`, `nuevo`; `dto/request/CreditoRequest.java`, `dto/response/CreditoResponse.java` (`cargos`) | **Decorator** (sobre la Strategy de M9; + OCP, SRP) |
| M13 | La autenticación se reconoce y nombra como Facade: `AuthService` ofrece `registrar`, `login` y `actual` sobre `AuthenticationManager`, `PasswordEncoder`, `TokenService` (Adapter M7) y `UsuarioRepository`. Ningún controlador usa ese subsistema (`git grep` en `controller/`). `AuthController.actual` recibe `UsuarioDetails` como los demás | `service/AuthService.java`; `service/impl/AuthServiceImpl.java`; `controller/AuthController.java` `actual` | **Facade** (+ Low Coupling, Protected Variations) |
| M14 | Regla de mora: no se otorga un crédito a un cliente con cuotas vencidas (400) | `service/impl/CreditoServiceImpl.java` `crear`; test `backend/src/test/java/com/uade/tpejemplo/service/ReglaDeMoraTest.java` | Composite (segundo cliente) |
| M15 | Deuda del cliente: `GET /api/clientes/{dni}/deuda` y saldo/vencido en la búsqueda por DNI (UC21) | `controller/ClienteController.java` `deuda`; `service/impl/ClienteServiceImpl.java` `deuda`; `dto/response/DeudaClienteResponse.java`; `frontend/src/pages/Clientes.jsx`, `frontend/src/api/clientes.js` `getDeudaCliente`; test `backend/src/test/java/com/uade/tpejemplo/service/DeudaClienteTest.java` | Composite (tercer cliente), MVC |
| M16 | Cargos en el alta de crédito (casillas) y en la ficha | `frontend/src/pages/Creditos.jsx`; `frontend/src/utils/cargos.js` | Decorator (vista) |
| DER4 | DER v4: tabla `credito_cargos` (PK compuesta `id_credito`, `tipo_cargo`) y `rol` como enum | `docs/diagramas/der-v4.py` | Documentación |
| CU4 | UC07 con cargos y error 400 por mora; UC21 Consultar deuda del cliente; `verificar-cu.sh` con el caso de mora | `docs/casos-de-uso/README.md`, `docs/casos-de-uso/verificar-cu.sh`, `docs/casos-de-uso/casos-de-uso-v4.puml` | Documentación |

Convenciones nuevas: `Deuda` va sin prefijo `I` porque nombra un rol de patrón (como `CalculoDeCuota`); `Cartera` no tiene `ICartera` porque no es entidad y su interfaz es `Deuda`; `TipoCargo` es, como `TipoPlan`, un enum registro (de decoradores).

## Foco de la consigna: Strategy y Adapter

| ID | Qué | Clase y método | Patrón / concepto |
|---|---|---|---|
| M9 | Cálculo de cuota intercambiable: interés simple y sistema francés. Context `Credito`: guarda `tipoPlan`, que le entrega la Strategy (`TipoPlan.calculo()`); elige quien otorga (`TipoPlan`); sin `setEstrategia` (cambiaría cuotas emitidas). Revierte el descarte del 15/09: Strategy necesita dos algoritmos reales | `model/interfaces/CalculoDeCuota.java`; `model/plan/InteresSimple.java`, `model/plan/SistemaFrances.java`; `model/TipoPlan.java`; `model/Credito.java` (constructor, `calculo`, `totalADevolver`) | **Strategy** (+ OCP, Polymorphism) |
| M7 | `UsuarioDetails` adapta `IUsuario` a `UserDetails` (ejemplo de manual). Target `TokenService`, Adapter `JwtUtil` que contiene el `JwtParser` de jjwt (y su `SecretKey`) como campos `final`; Adaptee: jjwt (`JwtParser`, `Jwts`) (Object Adapter) | `service/TokenService.java`; `security/JwtUtil.java`; clientes `security/JwtAuthFilter.java`, `service/impl/AuthServiceImpl.java`; `security/UsuarioDetails.java` | **Adapter** (+ DIP, Protected Variations) |

## Convenciones: excepciones a propósito

| Clase | Excepción | Motivo |
|---|---|---|
| `model/EstadoCredito.java` | sin `IEstadoCredito` | Enum de valores sin comportamiento |
| `model/TipoPlan.java` | sin `ITipoPlan` | Enum registro de estrategias |
| `model/interfaces/CalculoDeCuota.java` | sin prefijo `I` | Interfaz Strategy: nombre de rol |
| `service/TokenService.java` → `security/JwtUtil.java` | no es `TokenServiceImpl` | Es el Adapter; infraestructura, no caso de uso |
| `model/interfaces/IRol.java` | sin consumidor | Existe por la convención; una interfaz sin clientes no viola ISP |

## Hechos

| ID | Qué | Clase y método | Patrón / concepto |
|---|---|---|---|
| M1 | Rechazar el cobro sobre un crédito anulado (TPO-004) | `model/Cuota.java` `registrarCobranza` | Information Expert |
| M2 | "Tiene cobranzas" ignora las anuladas (absorbida por M8) | `model/Credito.java` `tieneCobranzas`; se borra `repository/CobranzaRepository.java` `existeCobranzaDelCredito` | Information Expert |
| M3 | Dashboard: activos = `estado()` VIGENTE (modelo); financiado y cobrado = lo no anulado (query; el financiado incluye cancelados) (TPO-003) | `service/impl/DashboardServiceImpl.java` `obtenerEstadisticasGenerales`; `repository/CreditoRepository.java` `sumarDeudaOriginalNoAnulada`; `repository/CobranzaRepository.java` `sumarImporteNoAnulado` | Corrección + Expert |
| M4 | Permisos de anulación validados en el backend (TPO-007) | `controller/CreditoController.java` `anularCredito`, `service/impl/CreditoServiceImpl.java` `anularCredito`, `service/impl/CobranzaServiceImpl.java` `anularCobranza` | MVC + Information Expert |
| M5 | Handlers 401/403/404/405 (TPO-001, TPO-002) | `exception/GlobalExceptionHandler.java` `handleNoAutenticado`, `handleAccesoDenegado`, `handleRutaNoEncontrada`, `handleMetodoNoPermitido` | MVC |
| M6 | Cuota vencida (mora) | `model/Cuota.java` `estaVencida`; `dto/response/CuotaResponse.java` | Information Expert |
| M8 | EstadoCredito y Credito como agregado (estado, saldo, anulación) | `model/EstadoCredito.java`; `model/Credito.java` `estado`, `saldo`, `estaCancelado`, `puedeAnularse`, `anular`; `service/impl/CreditoServiceImpl.java` `buscarCredito` | Information Expert |
| M10 | Dashboard también para ADMIN (TPO-010) | `config/SecurityConfig.java` `filterChain`; `frontend/src/pages/Dashboard.jsx`, `frontend/src/components/Navbar.jsx` | Corrección (control de acceso), sin patrón |
| API | Swagger, 401 sin token, `@Valid` en admin/supervisor, `GET /api/creditos` | `config/OpenApiConfig.java`; `config/SecurityConfig.java`; `controller/AdminController.java`, `controller/SupervisorController.java`; `service/impl/CreditoServiceImpl.java` `listarTodos` | MVC (contrato del controlador) |
| O1 | La vista usa `puedeAnularse` del modelo y recarga tras anular | `dto/response/CreditoResponse.java`; `frontend/src/pages/Creditos.jsx`; `frontend/src/store/slices/creditosSlice.js` | MVC + Expert |
| O2 | El Adapter traduce las excepciones de jjwt; Target sin tipos externos | `security/JwtUtil.java` `extraerUsername`; `security/JwtAuthFilter.java` | Adapter, Protected Variations |
| O3 | El total sale de las cuotas emitidas | `model/Credito.java` `totalADevolver`; `model/interfaces/CalculoDeCuota.java` | Strategy (alcance), Expert |
| O4 | "Créditos activos" con `Credito.estado()` | `service/impl/DashboardServiceImpl.java` `obtenerEstadisticasGenerales` | Information Expert |
| O5 | La guarda del ADMIN vive en el modelo | `model/Usuario.java` `otorgarPermisos`, `asignarRol`; `service/impl/AdminServiceImpl.java` | MVC + Expert |
| S1 | Transacciones en los services | `service/impl/AdminServiceImpl.java`, `AuthServiceImpl.java`, `ClienteServiceImpl.java`, `CobranzaServiceImpl.java` | Spring: `@Transactional` |
| S2 | open-in-view apagado | `backend/src/main/resources/application.properties` | Spring JPA |
| S3 | `jwt.secret` desde `JWT_SECRET` (TPO-009); prod la exige sin default | `backend/src/main/resources/application.properties`, `application-prod.properties` | Configuración fuera del código |
| S4 | El 500 no filtra el mensaje interno y se loguea | `exception/GlobalExceptionHandler.java` `handleGeneral` | Seguridad / MVC |
| S7-S9 | Perfiles dev/prod, CORS con `CORS_ORIGIN` y consola H2 solo en dev (TPO-008) | `application-dev.properties`, `application-prod.properties`; `config/SecurityConfig.java` `corsConfigurationSource` | Configuración externalizada |
| D1 | Borrar `findByRol` sin uso | `repository/UsuarioRepository.java` | Código muerto |
| D2 | Borrar `@Builder`/`@NoArgsConstructor` sin uso | `dto/response/AuthResponse.java` | Código muerto |
| BS1 | Supervisor lista usuarios sin admins; se borra `listarTodos` | `controller/SupervisorController.java`; `service/AdminService.java`; `service/impl/AdminServiceImpl.java`; `frontend/src/pages/GestorPermisos.jsx` | Código duplicado |
| DF1 | Borrar store huérfano | `frontend/src/store/index.js` | Código muerto (front) |
| DF3 | Sacar `console.log` que imprimía el JWT | `frontend/src/pages/Creditos.jsx` | Código de depuración |
| BSF1 | Borrar comentarios de copy-paste | `frontend/src/store/slices/permisosSlice.js`, `cobranzasSlice.js`; `frontend/src/pages/GestorPermisos.jsx` | Comentarios |
| F1 | Usuario autenticado sin el rol recibe 403, no 401 | `config/SecurityConfig.java` `filterChain` (`accessDeniedHandler`) | MVC (controlador) |
| F2 | Body mal formado o parámetro de tipo incorrecto responde 400, no 500 | `exception/GlobalExceptionHandler.java` `handleSolicitudInvalida` | MVC (controlador) |
| UI | Moneda y fechas es-AR, crédito con badge de estado y datos rotulados, fecha de cobranza, todos los mensajes de error, tabla de clientes | `frontend/src/utils/formato.js`; `frontend/src/pages/Creditos.jsx`, `Cobranzas.jsx`, `Clientes.jsx`, `Dashboard.jsx`; `frontend/src/api/apiClient.js` | Vista (MVC) |
| DT | Dark theme con variables CSS semánticas y `prefers-color-scheme` | `frontend/src/index.css`; `frontend/src/pages/*.jsx`, `frontend/src/components/Navbar.jsx` | Protected Variations (estilo) |
| COM | Comentarios: 1-3 líneas por clase, una línea por patrón, una por componente del front | `backend/src/main/java/**`, `frontend/src/**` | Bad smell: comentarios |
| LIM | Limpieza del repo: `.factorypath` sin trackear, `docs/trabajo` fuera del `git archive`, docs de proceso borrados, README corto | `.gitattributes`, `README.md`, `docs/` | Entrega |
| CU | Casos de uso V3: fichas, diagrama, trazabilidad y verificación por API | `docs/casos-de-uso/README.md`, `docs/casos-de-uso/verificar-cu.sh` | Documentación |
| H1 | Anular una cobranza ya anulada se rechaza (400), como el crédito | `model/Cobranza.java` `anular` | Information Expert |
| O6 | El crédito nace con su plan de cuotas: el constructor las genera y JPA las guarda por cascade; `generarPlanDeCuotas` pasa a privado y sale de `ICredito` | `model/Credito.java` constructor, `generarPlanDeCuotas`; `model/interfaces/ICredito.java`; `service/impl/CreditoServiceImpl.java` `crear` | Creator |
| O7 | Saldo pendiente y monto vencido en el dashboard; cada crédito suma su saldo y su vencido | `model/Credito.java` `saldo`, `montoVencido`; `service/impl/DashboardServiceImpl.java` `obtenerEstadisticasGenerales`; `dto/response/DashboardStatsResponse.java`; `frontend/src/pages/Dashboard.jsx` | Information Expert |
| S5 | Spring arma el `DaoAuthenticationProvider` con los beans `UserDetailsService` y `PasswordEncoder`; se borra el armado a mano y el warning del arranque | `config/SecurityConfig.java` | Spring Security |
| BS2 | El `ErrorResponse` se arma en un solo método; el código sale del `HttpStatus` | `exception/GlobalExceptionHandler.java` `error` | Código duplicado |
| I-2 | Largo y escala acotados en los request: lo que no entra en la columna da 400 y no 500 ni un redondeo silencioso | `dto/request/ClienteRequest.java`, `CreditoRequest.java`, `CobranzaRequest.java`, `RegisterRequest.java` (`@Size`, `@Digits`) | Validación en el controlador (MVC) |
| T-1 | Tests de los códigos HTTP 400/401/403/404/405 con la seguridad JWT real y los servicios simulados | `backend/src/test/java/com/uade/tpejemplo/controller/CodigosHttpTest.java` (`@WebMvcTest`) | Tests |
| T-5 | La fecha de hoy entra por parámetro: se testea anular una cobranza de otro día y el vencimiento sin depender del reloj | `model/Cobranza.java` `anular(LocalDate)`; `model/Cuota.java` `estaVencida(LocalDate)` | Tests |
| DER | Diagrama entidad-relación de la base, con claves y restricciones | `docs/diagramas/der-v3.py` en V3; hoy `docs/diagramas/der-v4.py` (genera `.svg` y `.png`) | Documentación |
| Tests | 62 tests, 0 fallas (43 en V3) | `backend/src/test/java/com/uade/tpejemplo/` | JUnit 5 + AssertJ, `@WebMvcTest` |

Desglose de los 62 tests:

- `model/CreditoTest`: 10
- `model/CuotaTest`: 12
- `model/CarteraTest`: 4 (M11)
- `model/CobranzaTest`: 3
- `model/plan/InteresSimpleTest`: 4
- `model/plan/SistemaFrancesTest`: 4
- `model/plan/SeguroDeVidaTest`: 2 (M12)
- `model/plan/GastoDeAdministracionTest`: 2 (M12)
- `model/plan/CargosApiladosTest`: 2 (M12)
- `controller/CodigosHttpTest`: 11
- `service/CobranzaConcurrenteTest`: 1 (I-1)
- `service/AnulacionConcurrenteTest`: 1 (I1)
- `service/ReglaDeMoraTest`: 2 (M14)
- `service/DeudaClienteTest`: 3 (M15)
- `TpEjemploApplicationTests` (`contextLoads`): 1

Decisión S6: el control de roles queda doble a propósito, reglas de URL en `config/SecurityConfig.java` `filterChain` y `@PreAuthorize` en `controller/AdminController.java` y `controller/SupervisorController.java`. Es defensa en profundidad: si una ruta cambia y la regla de URL deja de cubrirla, el método sigue protegido.

## Mejoras de la revisión

| ID | Qué | Clase y método | Patrón / concepto |
|---|---|---|---|
| I-1 | Dos cobros simultáneos de la misma cuota ya no dejan dos cobranzas vigentes | `repository/CuotaRepository.java` `buscarPorCreditoYNumero` (`@Lock(PESSIMISTIC_WRITE)`, sin `JOIN FETCH`); test `backend/src/test/java/com/uade/tpejemplo/service/CobranzaConcurrenteTest.java` | Concurrencia (bloqueo pesimista) |
| BS3 | El usuario responde si puede anular | `model/interfaces/IUsuario.java`, `model/Usuario.java` `puedeAnularCredito`, `puedeAnularCobranza`; usados en `service/impl/CreditoServiceImpl.java` `anularCredito` y `service/impl/CobranzaServiceImpl.java` `anularCobranza` | Information Expert / Tell, don't ask |
| BS4 | `rol` tipado como `Rol` en las respuestas | `dto/response/UsuarioResponse.java`, `dto/response/AuthResponse.java` | Primitive obsession |
| BS6 | Mismo verbo para el mismo caso | `controller/SupervisorController.java` `listarUsuarios` | Nombres consistentes |
| BS8 | Los permisos son `Boolean` con `@NotNull`: un body `{}` da 400 en vez de borrar los dos permisos con 200 | `dto/request/PermisosRequest.java`; `service/impl/AdminServiceImpl.java` `actualizarPermisos`; test `backend/src/test/java/com/uade/tpejemplo/controller/CodigosHttpTest.java` | Primitive obsession |
| BS9 | Comentarios que narraban código que ya no existe (builder, `findByDni`) o repetían en prosa la línea del patrón | `model/Usuario.java` `nuevo`; `repository/ClienteRepository.java`; `model/Cobranza.java` `anular`; `model/Cuota.java` `estaPagada`, `registrarCobranza` | Comentarios |
| BS10 | El 404 se lee bien: "No se encontró crédito con id 5" en vez de "Crédito no encontrado con id: '5'" (y "Cobranza no encontrado"); la cuota se busca por "crédito/cuota", no por el nombre técnico de los campos | `exception/ResourceNotFoundException.java`; `service/impl/CobranzaServiceImpl.java` `registrar` y los demás llamadores en `service/impl/` | Mensajes de error (como R2) |
| BS11 | `saldo` y `montoVencido` repetían el mismo filtrar-y-sumar importes de cuotas; queda un solo `sumarCuotas(filtro)` | `model/Credito.java` `saldo`, `montoVencido`, `sumarCuotas`; `docs/diagramas/clases-v4.puml` | Código duplicado |
| BS12 | Código muerto: `esValido` era tautológico (comparaba el usuario del token con el que se sacó de ese mismo token); el parseo ya valida firma y vencimiento | `service/TokenService.java`; `security/JwtUtil.java`; `security/JwtAuthFilter.java` `autenticar`; `docs/diagramas/clases-v4.puml`, `docs/diagramas/archify/adapter-jwt.sequence.json`, `docs/reporte/puml/m7-despues.puml` | Código muerto (Adapter: Target más chico) |
| BS13 | Precarga por efecto lateral: consultas cuyo resultado se descartaba; ahora lazy loading dentro de la transacción | `service/impl/CreditoServiceImpl.java` `listarTodos`, `listarPorCliente`, `anularCredito`, `buscarCredito`; `service/impl/DashboardServiceImpl.java` `obtenerEstadisticasGenerales`; `repository/CreditoRepository.java`; `repository/CuotaRepository.java`; `docs/diagramas/clases-v4.puml`, `docs/diagramas/archify/anular-credito.sequence.json`, `docs/reporte/puml/m3-despues.puml`, `docs/reporte/puml/m8-despues.puml` | Código muerto (efecto lateral oculto) |
| BS14 | El DNI acepta solo 7 u 8 dígitos: antes se podía registrar un cliente con DNI "sdfg" (UC04, DNI sin formato) | `dto/request/ClienteRequest.java`, `dto/request/CreditoRequest.java` (`@Pattern`); test `backend/src/test/java/com/uade/tpejemplo/controller/CodigosHttpTest.java` `dniConLetrasDa400` | Validación en el borde (MVC) |
| I1 | Anular y cobrar en paralelo ya no deja un crédito ANULADO con cobranza vigente: los dos caminos bloquean primero el crédito; cobrar además bloquea la cuota | `repository/CreditoRepository.java` (`@Lock(PESSIMISTIC_WRITE)`), `service/impl/CreditoServiceImpl.java` `anularCredito`, `service/impl/CobranzaServiceImpl.java` `registrar`; test `backend/src/test/java/com/uade/tpejemplo/service/AnulacionConcurrenteTest.java` | Concurrencia (bloqueo pesimista) |
| I3 | `Content-Type` no JSON → 415; `Accept` no JSON → 406, con `ErrorResponse` (antes 500) | `exception/GlobalExceptionHandler.java`; test `backend/src/test/java/com/uade/tpejemplo/controller/CodigosHttpTest.java` | Error del cliente = 4xx |
| I4 | Un alta ya no aparece en la lista de otro cliente o crédito buscado | `frontend/src/store/slices/creditosSlice.js`, `frontend/src/store/slices/cobranzasSlice.js` | MVC: la vista muestra lo que dice el modelo |
| Front | `RoleRoute` y `ROLES`, toda la API por `src/api`, `ESTADO_CUOTA`, sin restos de Vite (TPO-012) | `frontend/src/components/RoleRoute.jsx`, `frontend/src/utils/roles.js`, `frontend/src/api/dashboard.js`, `frontend/src/pages/Creditos.jsx` `estadoCuota` | Duplicated code, strings mágicos |

**I-1. Cobros simultáneos.**
- Problema: `service/impl/CobranzaServiceImpl.java` `registrar` leía la cuota, `model/Cuota.java` `registrarCobranza` la veía impaga y recién después insertaba. Dos requests a la vez veían la cuota impaga y cobraban las dos.
- Qué se hizo: `@Lock(PESSIMISTIC_WRITE)` en `buscarPorCreditoYNumero` y fuera su `LEFT JOIN FETCH c.cobranzas`: con el join, H2 leía las cobranzas de antes del bloqueo y seguía duplicando. `CobranzaConcurrenteTest` lanza 20 hilos sobre la misma cuota y espera 1 vigente; sin el arreglo falla (10 vigentes).
- Por qué: el esquema no admite un `unique` porque las anuladas también cuentan; el bloqueo serializa solo los cobros de la misma cuota.

**S7-S9. Perfiles.**
- Problema: `application.properties` mezclaba lo de desarrollo con lo único, `.cors(Customizer.withDefaults())` no tenía `CorsConfigurationSource` y `/h2-console/**` estaba en `permitAll` y además en `web.ignoring()`: la consola quedaba abierta sin login en cualquier perfil.
- Qué se hizo: `dev` por defecto (consola H2, SQL en logs, `create-drop`, CORS para `http://localhost:5173`); `prod` sin consola, sin SQL, `update` y CORS solo para `CORS_ORIGIN`, obligatoria. En prod la consola se deshabilita (`spring.h2.console.enabled=false`) y se sacó el `permitAll` redundante; en dev sigue abierta a propósito, fuera de la cadena por `web.ignoring()`.
- Por qué: lo cómodo para la demo no llega a prod. S6 no se tocó: el doble control de roles queda a propósito.

## Recorrido de casos de uso por la UI (T33)

Salen de recorrer cada caso de uso por la UI (incluido H4).

| ID | Qué | Clase y método | Patrón / concepto |
|---|---|---|---|
| R1 | Aviso de éxito al crear crédito o cobranza; el error de búsqueda queda en el bloque de búsqueda | `frontend/src/components/Aviso.jsx`; `frontend/src/pages/Creditos.jsx` `buscar`, `handleSubmit`; `Cobranzas.jsx` `buscar`, `handleSubmit`; `creditosSlice.js`, `cobranzasSlice.js` (`fetch...rejected`) | Vista (MVC) |
| R2 | Validación sin prefijo de campo; título del dashboard como el resto | `exception/GlobalExceptionHandler.java` `handleValidacion`; `frontend/src/pages/Dashboard.jsx` | Vista (MVC) |
| R3 | `GET /api/auth/me` y refresco de permisos al entrar a cada pantalla (H4) | `controller/AuthController.java` `actual`; `service/impl/AuthServiceImpl.java` `actual`; `config/SecurityConfig.java`; `frontend/src/components/PrivateRoute.jsx`; `authSlice.js` `refrescarUsuario` | MVC + Information Expert |
| R4 | Pantallas de UC06 y UC09 | `frontend/src/pages/Clientes.jsx` `buscar`; `frontend/src/pages/Creditos.jsx` `consultar`, `fichaCredito` | Vista (MVC) |

**R1. Avisos bien ubicados.**
- Problema (UC07, UC10, UC13): al buscar un DNI inexistente, el "Cliente no encontrado" aparecía dentro de "Nuevo crédito" y además se listaba "Créditos del cliente (0)", como si el cliente existiera. Y al crear un crédito o una cobranza el formulario se vaciaba sin decir nada: el usuario no sabía si se había guardado ni con qué número.
- Qué se hizo: la búsqueda guarda su propio error y lo muestra en su bloque; si falla no se dibuja el listado. El `error` del slice queda solo para el alta. Al crear se muestra "Crédito #N creado..." o "Cobranza #N registrada...". El cuadrito de aviso es un componente `Aviso` porque se repite en tres pantallas.
- Por qué es lo mínimo: no hay toasts ni librería, ni estado global nuevo; es un `useState` por pantalla y un componente de 8 líneas.

**R2. Mensajes limpios.**
- Problema (UC01, UC16): el registro mostraba "password: La contraseña debe tener al menos 6 caracteres", con el nombre técnico del campo, y el dashboard tenía el título centrado y pegado a las tarjetas, distinto al resto.
- Qué se hizo: el handler de validación devuelve solo el mensaje (que ya está escrito en castellano en cada `@NotBlank`/`@Size`). El dashboard usa el mismo contenedor y título que las otras pantallas.
- Por qué es lo mínimo: se arregla en el único lugar donde se arma el texto (el back), en vez de recortar strings en cada pantalla del front.

**R3. Permisos sin reloguear (H4).**
- Problema (UC19, UC11, UC15): el front leía rol y permisos de `localStorage` al loguearse. Si el supervisor le daba "anular crédito" a alguien, esa persona no veía el botón Anular hasta cerrar sesión y volver a entrar.
- Qué se hizo: `GET /api/auth/me` devuelve el usuario autenticado con rol y permisos, reusando `AuthResponse` (con `token` en null, porque no se emite uno nuevo). `PrivateRoute`, la guarda por la que pasa toda pantalla con sesión, lo pide al cargar la app y en cada cambio de pantalla, y actualiza el store conservando el token. Verificado: con `user` en Créditos, el supervisor le da y le saca el permiso por API, `user` navega Cobranzas → Créditos sin recargar y el botón aparece y desaparece.
- Por qué es lo mínimo: una consulta por navegación, sin polling ni websockets. Se pone en `PrivateRoute` y no en cada pantalla porque es el único punto por el que pasan todas. El back sigue siendo la autoridad (M4): esto solo evita que la vista quede desactualizada.

**R4. UC06 y UC09 con pantalla.**
- Problema (UC06, UC09): los dos casos de uso existían en el back y en `api/clientes.js` / `api/creditos.js`, pero ninguna pantalla los usaba.
- Qué se hizo: en Clientes, "Buscar cliente por DNI" muestra nombre y DNI o el 404. En Créditos, "Consultar crédito por número" muestra la misma ficha del listado (estado, plan, saldo, cuotas, Anular), extraída a `fichaCredito` para no duplicarla.
- Por qué es lo mínimo: usa los endpoints y funciones de API que ya estaban, sin slice nuevo (el resultado no lo comparte ninguna otra pantalla), y reutiliza la ficha del crédito en vez de escribir otra.

## Pendientes

| ID | Qué | Dónde | Por qué queda |
|---|---|---|---|
| H5 | `GET /api/creditos/{id}`, `/api/creditos/cliente/{dni}` y `/api/creditos` dan 500: `LazyInitializationException` sobre `Credito.cargos` al serializar | `dto/response/CreditoResponse.java` `desde` | Bug de M12 hallado con `verificar-cu.sh`; fix en el backend |
| O8 | Pagos parciales | `model/Cuota.java` `estaPagada` y todo lo que depende | Tamaño L, fuera de alcance de V3 y V4 |
| O3b | Ajustar la última cuota para que el total cierre exacto | `model/plan/InteresSimple.java`, `model/plan/SistemaFrances.java` | Hoy el centavo se explica como redondeo por cuota |
| S10 | Renombrar paquete `com.uade.tpejemplo` (TPO-014) | todo el backend | Diff ruidoso; evaluar para el 17/11 |
| T-2, T-3 | `@DataJpaTest` del dashboard, `Permisos` | `backend/src/test` | Siguiente iteración |
| H8 | Trazabilidad: qué usuario cobró o anuló | `model/Cobranza.java`, `model/Credito.java` | Modelo nuevo y cambio de API |
| Base prod | Prod sigue con H2 en memoria: sin persistencia ni alta de admin | `application-prod.properties` | Falta base real (PostgreSQL) y migraciones |
| P-M9 | Tasa con unidad declarada por plan; cuota con capital/interés separados | `model/TipoPlan.java`, `model/Cuota.java` | Límites del Strategy actual; sin el desglose tampoco hay IVA sobre intereses como decorador |
| M12b | Seguro de vida sobre el saldo deudor | `model/plan/SeguroDeVida.java` | Simplificación de M12: hoy se calcula sobre el capital |

Descartados con motivo: `EstadoCredito` como State (O9: es un valor derivado) y Strategy/Adapter "para mostrar" (O10: no tapan huecos).
