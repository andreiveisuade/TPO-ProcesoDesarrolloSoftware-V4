#!/bin/bash
# Recorre por API el flujo principal y los alternativos de cada caso de uso (docs/casos-de-uso/README.md, sección 6).
# Asume una base recién levantada: los ids de créditos, cobranzas y usuarios salen del orden de creación.
# Uso: ./verificar-cu.sh  (o API=http://localhost:8093/api ./verificar-cu.sh)
H=${API:-http://localhost:8080/api}
tok() { curl -s -X POST $H/auth/login -H 'Content-Type: application/json' -d "{\"username\":\"$1\",\"password\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'; }
TMP=$(mktemp)
c() { # caso metodo path token [body]
  local caso=$1 m=$2 p=$3 t=$4 b=$5
  local args=(-s -o $TMP -w '%{http_code}' -X $m "$H$p" -H 'Content-Type: application/json')
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-d "$b")
  local code=$(curl "${args[@]}")
  printf '%-58s %s  %s\n' "$caso" "$code" "$(head -c 170 $TMP)"
}
echo "== UC01/UC02"
c "UC01 registrar nuevo" POST /auth/register "" '{"username":"op1","password":"secreto"}'
c "UC01 username repetido" POST /auth/register "" '{"username":"op1","password":"secreto"}'
c "UC01 password corta" POST /auth/register "" '{"username":"op2","password":"123"}'
c "UC02 login ok" POST /auth/login "" '{"username":"user","password":"user"}'
c "UC02 password incorrecta" POST /auth/login "" '{"username":"user","password":"mal"}'
c "UC02 usuario inexistente" POST /auth/login "" '{"username":"nadie","password":"x"}'
c "UC02 campos vacios" POST /auth/login "" '{"username":"","password":""}'
A=$(tok admin admin); S=$(tok supervisor supervisor); U=$(tok user user); O=$(tok op1 secreto)
echo "== UC04-06"
c "sin token" GET /clientes ""
c "UC04 crear cliente" POST /clientes $U '{"dni":"30111222","nombre":"Ana Perez"}'
c "UC04 DNI repetido" POST /clientes $U '{"dni":"30111222","nombre":"Otra"}'
c "UC04 sin nombre" POST /clientes $U '{"dni":"1","nombre":""}'
c "UC04 DNI no numerico (BS14)" POST /clientes $U '{"dni":"abc","nombre":"X"}'
c "UC04 cliente que va a quedar en mora" POST /clientes $U '{"dni":"30999888","nombre":"Beto Mora"}'
c "UC05 listar" GET /clientes $U
c "UC06 buscar ok" GET /clientes/30111222 $U
c "UC06 inexistente" GET /clientes/999 $U
echo "== UC07-10"
c "UC07 credito simple" POST /creditos $U '{"dniCliente":"30111222","deudaOriginal":1200,"fecha":"2026-10-06","tasaInteres":10,"cantidadCuotas":3,"tipoPlan":"INTERES_SIMPLE"}'
c "UC07 credito frances" POST /creditos $U '{"dniCliente":"30111222","deudaOriginal":1200,"fecha":"2026-10-06","tasaInteres":10,"cantidadCuotas":3,"tipoPlan":"SISTEMA_FRANCES"}'
c "UC07 credito vencido (fecha pasada, otro cliente)" POST /creditos $U '{"dniCliente":"30999888","deudaOriginal":500,"fecha":"2025-01-01","tasaInteres":0,"cantidadCuotas":2,"tipoPlan":"INTERES_SIMPLE"}'
c "UC07 fecha futura (aceptada)" POST /creditos $U '{"dniCliente":"30111222","deudaOriginal":100,"fecha":"2030-01-01","tasaInteres":0,"cantidadCuotas":1,"tipoPlan":"INTERES_SIMPLE"}'
c "UC07 cliente inexistente" POST /creditos $U '{"dniCliente":"99999999","deudaOriginal":1,"fecha":"2026-10-06","tasaInteres":1,"cantidadCuotas":1,"tipoPlan":"INTERES_SIMPLE"}'
c "UC07 deuda 0 y cuotas 0" POST /creditos $U '{"dniCliente":"30111222","deudaOriginal":0,"fecha":"2026-10-06","tasaInteres":1,"cantidadCuotas":0,"tipoPlan":"INTERES_SIMPLE"}'
c "UC07 tipoPlan invalido" POST /creditos $U '{"dniCliente":"30111222","deudaOriginal":1,"fecha":"2026-10-06","tasaInteres":1,"cantidadCuotas":1,"tipoPlan":"OTRO"}'
c "UC07 tasa 1000" POST /creditos $U '{"dniCliente":"30111222","deudaOriginal":1,"fecha":"2026-10-06","tasaInteres":1000,"cantidadCuotas":1,"tipoPlan":"INTERES_SIMPLE"}'
c "UC07 cliente en mora" POST /creditos $U '{"dniCliente":"30999888","deudaOriginal":100,"fecha":"2026-10-06","tasaInteres":0,"cantidadCuotas":1,"tipoPlan":"INTERES_SIMPLE"}'
c "UC07 con seguro y gastos (cuota 50600)" POST /creditos $U '{"dniCliente":"30111222","deudaOriginal":100000,"fecha":"2026-10-06","tasaInteres":0,"cantidadCuotas":2,"tipoPlan":"INTERES_SIMPLE","cargos":["SEGURO_DE_VIDA","GASTO_DE_ADMINISTRACION"]}'
c "UC07 cargo invalido" POST /creditos $U '{"dniCliente":"30111222","deudaOriginal":1,"fecha":"2026-10-06","tasaInteres":0,"cantidadCuotas":1,"tipoPlan":"INTERES_SIMPLE","cargos":["IVA"]}'
echo "== UC21"
c "UC21 deuda al dia" GET /clientes/30111222/deuda $U
c "UC21 deuda en mora" GET /clientes/30999888/deuda $U
c "UC21 cliente inexistente" GET /clientes/99999999/deuda $U
c "UC21 sin token" GET /clientes/30111222/deuda ""
c "UC09 consultar 1" GET /creditos/1 $U
c "UC09 inexistente" GET /creditos/999 $U
c "UC09 id no numerico" GET /creditos/abc $U
c "UC10 por cliente" GET /creditos/cliente/30111222 $U
c "UC10 cliente inexistente" GET /creditos/cliente/999 $U
c "GET /creditos (sin CU)" GET /creditos $U
echo "== UC13-15"
c "UC13 cobrar cuota 1 cred 1" POST /cobranzas $U '{"idCredito":1,"numeroCuota":1,"importe":440}'
c "UC13 cobrar de nuevo" POST /cobranzas $U '{"idCredito":1,"numeroCuota":1,"importe":440}'
c "UC13 importe distinto" POST /cobranzas $U '{"idCredito":1,"numeroCuota":2,"importe":100}'
c "UC13 cuota inexistente" POST /cobranzas $U '{"idCredito":1,"numeroCuota":9,"importe":440}'
c "UC13 importe negativo" POST /cobranzas $U '{"idCredito":1,"numeroCuota":2,"importe":-1}'
c "UC14 listar cobranzas cred 1" GET /cobranzas/credito/1 $U
c "UC14 credito inexistente" GET /cobranzas/credito/999 $U
c "UC11 anular con cobranzas" DELETE /creditos/anular/1 $S
c "UC11 user sin permiso" DELETE /creditos/anular/2 $U
c "UC11 recien registrado sin permiso" DELETE /creditos/anular/2 $O
c "UC11 supervisor ok" DELETE /creditos/anular/2 $S
c "UC11 ya anulado" DELETE /creditos/anular/2 $S
c "UC11 inexistente" DELETE /creditos/anular/999 $S
c "UC13 cobrar credito anulado" POST /cobranzas $U '{"idCredito":2,"numeroCuota":1,"importe":482.54}'
c "UC15 user sin permiso" DELETE /cobranzas/1 $U
c "UC15 supervisor ok" DELETE /cobranzas/1 $S
c "UC15 anular otra vez la misma" DELETE /cobranzas/1 $S
c "UC15 inexistente" DELETE /cobranzas/999 $S
c "UC13 recobrar cuota liberada" POST /cobranzas $U '{"idCredito":1,"numeroCuota":1,"importe":440}'
c "UC09 cuotas del credito vencido (3)" GET /creditos/3 $U
echo "== UC16"
c "UC16 supervisor" GET /dashboard/stats $S
c "UC16 admin" GET /dashboard/stats $A
c "UC16 user" GET /dashboard/stats $U
echo "== UC18-20"
c "UC18 admin lista" GET /admin/usuarios $A
c "UC18 supervisor lista" GET /supervisor/usuarios $S
c "UC18 user a supervisor" GET /supervisor/usuarios $U
c "UC18 supervisor a admin" GET /admin/usuarios $S
c "UC19 sup da permiso anular credito a op1(id4)" PUT /supervisor/usuarios/4/permisos-anulacion $S '{"puedeAnularCredito":true,"puedeAnularCobranza":false}'
c "UC19 op1 anula cred 4 con el MISMO token" DELETE /creditos/anular/4 $O
c "UC19 sup modifica permisos del admin(id1)" PUT /supervisor/usuarios/1/permisos-anulacion $S '{"puedeAnularCredito":false,"puedeAnularCobranza":false}'
c "UC19 usuario inexistente" PUT /supervisor/usuarios/99/permisos-anulacion $S '{"puedeAnularCredito":true,"puedeAnularCobranza":true}'
c "UC19 via admin (sin pantalla)" PUT /admin/usuarios/3/permisos $A '{"puedeAnularCredito":true,"puedeAnularCobranza":true}'
c "UC20 admin: user->SUPERVISOR" PUT /admin/usuarios/4/rol $A '{"rol":"SUPERVISOR"}'
c "UC20 otorgar ADMIN" PUT /admin/usuarios/4/rol $A '{"rol":"ADMIN"}'
c "UC20 cambiar rol del admin" PUT /admin/usuarios/1/rol $A '{"rol":"USER"}'
c "UC20 rol invalido" PUT /admin/usuarios/4/rol $A '{"rol":"JEFE"}'
c "UC20 op1 ve dashboard con token viejo" GET /dashboard/stats $O
echo "== UC16 con un crédito cancelado"
for n in 1 2; do c "UC13 cobrar cuota $n del crédito 3 (vencido)" POST /cobranzas $U "{\"idCredito\":3,\"numeroCuota\":$n,\"importe\":250}"; done
c "UC09 crédito 3 queda CANCELADO" GET /creditos/3 $U
c "UC16 activos baja, financiado no" GET /dashboard/stats $S
c "UC21 el cliente ya no esta en mora" GET /clientes/30999888/deuda $U
c "UC07 al dia, se le vuelve a otorgar" POST /creditos $U '{"dniCliente":"30999888","deudaOriginal":100,"fecha":"2026-10-06","tasaInteres":0,"cantidadCuotas":1,"tipoPlan":"INTERES_SIMPLE"}'
