# DER V4: esquema físico que genera Hibernate sobre H2, columna a columna.
# Genera der-v4.svg y der-v4.png (python3 der-v4.py). Estilo: "Database schema" de diagram-design con la paleta del reporte.
import os, subprocess
from html import escape as e

AQUI = os.path.dirname(os.path.abspath(__file__))
INK, MUTED, RULE, PAPER = "#1D2B4F", "#555f73", "#D9E1EC", "#ffffff"
SANS = "Calibri, Carlito, 'Segoe UI', system-ui, sans-serif"
MONO = "'SF Mono', Menlo, Consolas, monospace"
W, HEAD, ROW = 260, 28, 24

# (columna, chips, tipo SQL)
TABLAS = {
    "clientes": (40, 40, [
        ("dni", ["PK"], "varchar(15)"),
        ("nombre", [], "varchar(255)"),
    ], None),
    "creditos": (372, 40, [
        ("id", ["PK"], "bigint identity"),
        ("dni_cliente", ["FK"], "varchar(15)"),
        ("deuda_original", [], "numeric(12,2)"),
        ("fecha", [], "date"),
        ("tasa_interes", [], "numeric(5,2)"),
        ("cantidad_cuotas", ["CK"], "integer ≥ 1"),
        ("tipo_plan", [], "varchar(20)"),
        ("anulado", [], "boolean"),
    ], None),
    "cuotas": (704, 40, [
        ("id", ["PK"], "bigint identity"),
        ("id_credito", ["FK"], "bigint"),
        ("numero", ["CK"], "integer ≥ 1"),
        ("importe", [], "numeric(12,2)"),
        ("fecha_vencimiento", [], "date"),
    ], "uq (id_credito, numero)"),
    "cobranzas": (1036, 40, [
        ("id", ["PK"], "bigint identity"),
        ("id_cuota", ["FK"], "bigint"),
        ("importe", [], "numeric(12,2)"),
        ("fecha_cobranza", [], "date"),
        ("anulada", [], "boolean"),
    ], None),
    "credito_cargos": (704, 262, [
        ("id_credito", ["PK", "FK"], "bigint"),
        ("tipo_cargo", ["PK"], "enum GASTO_DE_ADMINISTRACION/SEGURO_DE_VIDA"),
    ], "pk (id_credito, tipo_cargo)"),
    "usuarios": (40, 200, [
        ("id", ["PK"], "bigint identity"),
        ("username", ["UQ"], "varchar(255)"),
        ("password", [], "varchar(255) BCrypt"),
        ("rol", [], "enum ADMIN/SUPERVISOR/USER"),
        ("puede_anular_credito", [], "boolean"),
        ("puede_anular_cobranza", [], "boolean"),
        ("activo", [], "boolean default true"),
    ], None),
}
# Ancho propio solo donde el tipo no entra en W
ANCHO = {"credito_cargos": 400}
# FK hija.columna -> padre.columna, etiqueta de cardinalidad
# credito_cargos primero: su linea pasa por detras de la etiqueta de cuotas
FKS = [("credito_cargos", "id_credito", "creditos", "id", "1 : 0..N"),
       ("creditos", "dni_cliente", "clientes", "dni", "1 : 0..N"),
       ("cuotas", "id_credito", "creditos", "id", "1 : 1..N"),
       ("cobranzas", "id_cuota", "cuotas", "id", "1 : 0..N")]
VB_W, VB_H = 1336, 460


def fila_y(tabla, col):
    x, y, cols, _ = TABLAS[tabla]
    i = [c[0] for c in cols].index(col)
    return y + HEAD + i * ROW + ROW // 2


def chip(x, y, txt):
    return (f'<rect x="{x}" y="{y}" width="20" height="12" rx="2" fill="none" stroke="{INK}" stroke-opacity=".35" stroke-width=".8"/>'
            f'<text x="{x + 10}" y="{y + 9}" fill="{INK}" fill-opacity=".8" font-size="8" font-family="{MONO}" text-anchor="middle">{txt}</text>')


def tabla(nombre):
    x, y, cols, extra = TABLAS[nombre]
    ancho = ANCHO.get(nombre, W)
    h = HEAD + len(cols) * ROW + (28 if extra else 0)
    chip_x = x + 12 + max(len(c[0]) for c in cols if c[1]) * 7.4 + 10
    out = [f'<rect x="{x}" y="{y}" width="{ancho}" height="{h}" rx="6" fill="{PAPER}" stroke="{INK}"/>',
           f'<rect x="{x}" y="{y}" width="{ancho}" height="{HEAD}" rx="6" fill="{INK}" fill-opacity=".05"/>',
           f'<line x1="{x}" y1="{y + HEAD}" x2="{x + ancho}" y2="{y + HEAD}" stroke="{INK}" stroke-opacity=".22"/>',
           f'<text x="{x + 12}" y="{y + 18}" fill="{INK}" font-size="13" font-weight="700" font-family="{SANS}">{nombre}</text>',
           f'<rect x="{x + ancho - 52}" y="{y + 8}" width="40" height="12" rx="2" fill="none" stroke="{INK}" stroke-opacity=".4" stroke-width=".8"/>',
           f'<text x="{x + ancho - 32}" y="{y + 17}" fill="{INK}" fill-opacity=".8" font-size="8" font-family="{MONO}" text-anchor="middle" letter-spacing=".08em">TABLE</text>']
    for i, (col, chips, tipo) in enumerate(cols):
        ry = y + HEAD + i * ROW
        if i % 2 == 0:
            out.append(f'<rect x="{x + 1}" y="{ry}" width="{ancho - 2}" height="{ROW}" fill="{INK}" fill-opacity=".025"/>')
        out.append(f'<text x="{x + 12}" y="{ry + 16}" fill="{INK}" font-size="12" font-family="{SANS}">{col}</text>')
        for k, c in enumerate(chips):
            out.append(chip(chip_x + k * 24, ry + 6, c))
        out.append(f'<text x="{x + ancho - 12}" y="{ry + 16}" fill="{MUTED}" font-size="9" font-family="{MONO}" text-anchor="end">{e(tipo)}</text>')
    if extra:
        ry = y + HEAD + len(cols) * ROW
        out.append(f'<line x1="{x}" y1="{ry}" x2="{x + ancho}" y2="{ry}" stroke="{INK}" stroke-opacity=".12"/>')
        out.append(f'<text x="{x + 12}" y="{ry + 18}" fill="{MUTED}" font-size="9" font-family="{MONO}">{e(extra)}</text>')
    return "\n".join(out)


def fk(hija, col_h, padre, col_p, label):
    x1 = TABLAS[hija][0]
    y1 = fila_y(hija, col_h)
    x2 = TABLAS[padre][0] + W
    y2 = fila_y(padre, col_p)
    mx = (x1 + x2) // 2
    r = 8
    d = f"M {x1},{y1} H {mx + r} Q {mx},{y1} {mx},{y1 - r} V {y2 + r} Q {mx},{y2} {mx - r},{y2} H {x2 + 2}"
    lw = len(label) * 6 + 12
    return (f'<path d="{d}" fill="none" stroke="{MUTED}" stroke-width="1.2" marker-end="url(#der-v4-arrow)"/>'
            f'<rect x="{mx - lw // 2}" y="{y1 + 8}" width="{lw}" height="14" rx="2" fill="{PAPER}"/>'
            f'<text x="{mx}" y="{y1 + 18}" fill="{MUTED}" font-size="9" font-family="{MONO}" text-anchor="middle">{label}</text>')


def nota(x, y, lineas):
    t = "".join(f'<tspan x="{x}" dy="{0 if i == 0 else 15}">{e(l)}</tspan>' for i, l in enumerate(lineas))
    return (f'<line x1="{x - 10}" y1="{y - 11}" x2="{x - 10}" y2="{y + 15 * (len(lineas) - 1) + 4}" stroke="#2E75B6" stroke-width="2"/>'
            f'<text x="{x}" y="{y}" fill="{INK}" font-size="12" font-style="italic" font-family="{SANS}">{t}</text>')


partes = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {VB_W} {VB_H}" role="img" aria-labelledby="der-v4-title der-v4-desc">',
          '<title id="der-v4-title">DER V4: dashboard de préstamos</title>',
          '<desc id="der-v4-desc">Esquema físico de las seis tablas que genera Hibernate sobre H2, con tipos SQL, claves y cada clave foránea unida columna a columna.</desc>',
          f'<defs><marker id="der-v4-arrow" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="{MUTED}"/></marker></defs>',
          f'<rect width="100%" height="100%" fill="{PAPER}"/>']
partes += [fk(*f) for f in FKS]
partes += [tabla(t) for t in TABLAS]
partes.append(nota(382, 316, ["Sin columna de estado:",
                              "VIGENTE / CANCELADO / ANULADO lo calcula",
                              "Credito.estado() con anulado y las cobranzas."]))
partes.append(nota(1046, 220, ["A lo sumo una cobranza vigente por cuota:",
                               "el esquema no lo impide; lo valida",
                               "Cuota.registrarCobranza() con bloqueo."]))
ly = VB_H - 34
partes.append(f'<line x1="40" y1="{ly - 14}" x2="{VB_W - 40}" y2="{ly - 14}" stroke="{RULE}"/>')
leyenda = [("PK", "clave primaria"), ("FK", "clave foránea"), ("UQ", "única"), ("CK", "check")]
for i, (c, txt) in enumerate(leyenda):
    lx = 40 + i * 150
    partes.append(chip(lx, ly, c))
    partes.append(f'<text x="{lx + 28}" y="{ly + 10}" fill="{MUTED}" font-size="11" font-family="{SANS}">{txt}</text>')
partes.append(f'<text x="640" y="{ly + 10}" fill="{MUTED}" font-size="11" font-family="{SANS}">Todas las columnas NOT NULL. FK sin ON DELETE: no se borra nada, la anulación es lógica.</text>')
partes.append("</svg>")

open(os.path.join(AQUI, "der-v4.svg"), "w").write("\n".join(partes) + "\n")
subprocess.run(["rsvg-convert", "-z", "2", os.path.join(AQUI, "der-v4.svg"), "-o", os.path.join(AQUI, "der-v4.png")], check=True)
