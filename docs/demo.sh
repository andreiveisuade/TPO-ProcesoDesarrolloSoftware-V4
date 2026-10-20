#!/usr/bin/env bash
# Abre nvim con una tab por archivo cambiado (v2 a la izquierda, main a la derecha), en el orden de la presentación.
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$RAIZ"
B=backend/src/main/java/com/uade/tpejemplo
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
VIM="$TMP/demo.vim"

cambios=(
  "M9 Strategy|$B/model/credito/Credito.java $B/model/plan/CalculoDeCuota.java $B/model/plan/InteresSimple.java $B/model/plan/SistemaFrances.java $B/model/plan/TipoPlan.java"
  "M7 Adapter|$B/service/TokenService.java $B/security/JwtUtil.java $B/security/JwtAuthFilter.java $B/security/UsuarioDetails.java"
  "M8+O6 EstadoCredito, agregado y Creator|$B/model/credito/EstadoCredito.java $B/model/credito/Credito.java $B/model/credito/Cuota.java $B/service/impl/CreditoServiceImpl.java"
  "M4+BS3 permisos en el modelo|$B/controller/CreditoController.java $B/model/usuario/Usuario.java $B/service/impl/CreditoServiceImpl.java"
  "M5 errores, seguridad y perfiles|$B/exception/GlobalExceptionHandler.java $B/config/SecurityConfig.java $B/config/OpenApiConfig.java backend/src/main/resources/application-dev.properties backend/src/main/resources/application-prod.properties"
  "M3+O7 Dashboard|$B/service/impl/DashboardServiceImpl.java $B/dto/response/DashboardStatsResponse.java"
  "M1 Information Expert|$B/model/credito/Cuota.java"
  "M6 Cuota.estaVencida|$B/model/credito/Cuota.java"
  "M10 Dashboard para ADMIN|$B/config/SecurityConfig.java"
  "I-1 Bloqueo al cobrar|$B/repository/CuotaRepository.java $B/service/impl/CobranzaServiceImpl.java"
)

cat > "$VIM" <<'VIML'
set noswapfile hidden nofoldenable
set showtabline=2
function! TablineDemo()
  let s = ''
  for i in range(1, tabpagenr('$'))
    let s .= (i == tabpagenr() ? '%#TabLineSel#' : '%#TabLine#') . ' ' . get(gettabvar(i, ''), 'titulo', '') . ' '
  endfor
  return s . '%#TabLineFill#'
endfunction
set tabline=%!TablineDemo()
VIML

n=0
for cambio in "${cambios[@]}"; do
  titulo="${cambio%%|*}"
  for ruta in ${cambio#*|}; do
    [[ -f "$ruta" ]] || { echo "No existe: $ruta" >&2; exit 1; }
    n=$((n + 1))
    viejo="$TMP/$n-v2-$(basename "$ruta")"
    git show "v2:$ruta" > "$viejo" 2>/dev/null || : > "$viejo"
    cat >> "$VIM" <<VIML
$([[ $n -eq 1 ]] && echo "edit $viejo" || echo "tabedit $viejo")
let t:titulo = '$titulo: $(basename "$ruta")'
setlocal readonly nomodifiable
diffthis
vsplit $RAIZ/$ruta
diffthis
VIML
  done
done
echo "tabfirst" >> "$VIM"

if [[ "${1:-}" == "--headless" ]]; then
  nvim --headless -S "$VIM" +"echo tabpagenr('\$')" +qa
  echo
else
  nvim -S "$VIM"
fi
