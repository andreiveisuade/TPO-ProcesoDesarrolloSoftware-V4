import os, re, subprocess, sys, html
sys.path.insert(0, os.path.dirname(__file__))
from estilo import CSS

OUT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO = os.path.abspath(os.path.join(OUT, "..", ".."))
J = "backend/src/main/java/com/uade/tpejemplo/"
A, D = "v3", "iteracion4"
e = html.escape


def git(*args):
    return subprocess.run(["git", "-C", REPO, *args], capture_output=True, text=True).stdout


def show(c, path):
    return subprocess.run(["git", "-C", REPO, "show", f"{c}:{path}"], capture_output=True, text=True, check=True).stdout


def full(path):
    return path if path.startswith(("backend/", "frontend/")) else J + path


def method(c, path, sig):
    ls = show(c, full(path)).split("\n")
    i = next(k for k, l in enumerate(ls) if re.search(sig, l))
    s = i
    while s > 0 and ls[s - 1].strip().startswith(("@", "//")):
        s -= 1
    depth = 0; started = False; j = i
    while True:
        depth += ls[j].count("{") - ls[j].count("}")
        if "{" in ls[j]:
            started = True
        if started and depth == 0:
            break
        j += 1
    return "\n".join(ls[s:j + 1])


def lines(c, path, sig, n=1, before=0):
    ls = show(c, full(path)).split("\n")
    i = next(k for k, l in enumerate(ls) if re.search(sig, l))
    return "\n".join(ls[i - before:i + n])


def body(c, path):
    t = show(c, full(path))
    t = re.sub(r"^package .*?\n", "", t)
    t = re.sub(r"^import .*?\n", "", t, flags=re.M)
    return t.strip()


def cb(c, path, *partes):
    ver = "V3 (tag v3)" if c == A else "V4 (iteracion4)"
    return f'<div class="ruta">{e(full(path))} <span class="ver">· {ver}</span></div><pre><code>{e(chr(10).join(partes))}</code></pre>'


def svg(key, suf):
    return re.sub(r"<\?xml[^>]*\?>", "", open(f"{OUT}/puml/{key}-{suf}.svg").read())


def diagramas(key, tit_a="Antes (V3)", tit_d="Después (V4)"):
    out = '<div class="par">'
    for suf, tit in (("antes", tit_a), ("despues", tit_d)):
        src = open(f"{OUT}/puml/{key}-{suf}.puml").read()
        out += f'<figure><figcaption>{tit}</figcaption><div class="svgbox">{svg(key, suf)}</div>' \
               f'<details><summary>puml/{key}-{suf}.puml</summary><pre><code>{e(src)}</code></pre></details></figure>'
    return out + '</div>'


def archify_svg(n):
    # Prefija los id con el nombre del diagrama: varios SVG inline no pueden repetir id ni pisar anclas del reporte.
    t = open(os.path.join(REPO, f"docs/diagramas/archify/{n}.svg")).read()
    t = re.sub(r'\bid="([^"]+)"', rf'id="{n}-\1"', t)
    t = re.sub(r'url\(#([^)]+)\)', rf'url(#{n}-\1)', t)
    t = re.sub(r'(aria-labelledby|aria-describedby)="([^"]+)"', lambda m: m.group(1) + '="' + " ".join(f"{n}-{i}" for i in m.group(2).split()) + '"', t)
    t = re.sub(r'href="#([^"]+)"', rf'href="#{n}-\1"', t)
    return re.sub(r'<svg ', '<svg style="width:100%;height:auto" ', t, count=1)


def figura(n, titulo):
    return f'<figure style="margin-top:18px"><figcaption>{titulo}</figcaption><div class="mvcsvg">{archify_svg(n)}</div></figure>'


def roles(filas):
    return '<table><thead><tr><th>Rol</th><th>Clase</th></tr></thead><tbody>' + "".join(f"<tr><td><strong>{r}</strong></td><td>{c}</td></tr>" for r, c in filas) + '</tbody></table>'


def trampa(pregunta, respuesta):
    return f'<dl class="qa"><dt>Pregunta trampa: “{pregunta}”</dt><dd>{respuesta}</dd></dl>'


def bloque(id_, titulo, badge, patron, problema, antes, solucion, despues, por_que, consecuencias, ref="", extra=""):
    return f'''<article class="mejora" id="{id_}">
<h3>{titulo} <span class="badge impl">{badge}</span></h3>
{('<p class="ref">' + ref + '</p>') if ref else ''}
<dl class="estructura">
<dt>Patrón</dt><dd>{patron}</dd>
<dt>Problema (código antes)</dt><dd><p>{problema}</p>{antes}</dd>
<dt>Solución (código después)</dt><dd><p>{solucion}</p>{despues}</dd>
<dt>Por qué</dt><dd>{por_que}</dd>
<dt>Consecuencias</dt><dd>{consecuencias}</dd>
</dl>
{extra}
{diagramas(id_)}
</article>'''


# ---------------- M11 Composite ----------------
m11 = bloque("composite", "M11. Composite: saldo y mora a cualquier nivel", "implementada",
    "<strong>Composite</strong> (GoF, estructural): el cliente trata igual a una parte y a un conjunto.",
    "La misma pregunta (¿cuánto se debe?, ¿cuánto venció?) se contestaba con <strong>código distinto en cada nivel</strong>.",
    cb(A, "model/Credito.java", method(A, "model/Credito.java", r"public BigDecimal saldo"), "", method(A, "model/Credito.java", r"private BigDecimal sumarCuotas"))
    + cb(A, "service/impl/DashboardServiceImpl.java", lines(A, "service/impl/DashboardServiceImpl.java", r"// Information Expert: el saldo", 7)),
    "Interfaz <code>Deuda</code>: la cuota responde; crédito y cartera <strong>suman a sus hijos</strong>.",
    cb(D, "model/interfaces/Deuda.java", body(D, "model/interfaces/Deuda.java"))
    + cb(D, "model/Cuota.java", method(D, "model/Cuota.java", r"public BigDecimal saldo"), "", method(D, "model/Cuota.java", r"public BigDecimal montoVencido"))
    + cb(D, "model/Credito.java", method(D, "model/Credito.java", r"public BigDecimal saldo"), "", method(D, "model/Credito.java", r"private BigDecimal sumar"))
    + cb(D, "model/Cartera.java", body(D, "model/Cartera.java"))
    + cb(D, "service/impl/DashboardServiceImpl.java", lines(D, "service/impl/DashboardServiceImpl.java", r"// Composite: la cartera suma", 4)),
    "El árbol ya existía (cartera → crédito → cuota). <strong>Polymorphism</strong>, <strong>OCP</strong>.",
    "Gana: una sola suma, reutilizada por mora y deuda del cliente. Cuesta: <code>Credito</code> solo admite <code>Cuota</code> (JPA).",
    ref="Commits <code>7b47ccc</code>, <code>e51bb24</code>.",
    extra=roles([
        ("Component", "<code>model/interfaces/Deuda.java</code>"),
        ("Leaf", "<code>model/Cuota.java</code>"),
        ("Composite", "<code>model/Credito.java</code>, <code>model/Cartera.java</code>"),
        ("Client", "<code>DashboardServiceImpl</code>, <code>CreditoServiceImpl</code>, <code>ClienteServiceImpl</code>"),
    ]) + trampa("¿No es una lista sumada con un stream?",
        "No: <code>Cartera</code> es una <code>Deuda</code> y contiene créditos, que también son compuestos.")
    + figura("composite-deuda", "Secuencia: el dashboard pide el saldo a la cartera"))

# ---------------- M12 Decorator ----------------
m12 = bloque("decorator", "M12. Decorator: cargos sobre la cuota", "expansión del dominio",
    "<strong>Decorator</strong> (GoF, estructural): agrega responsabilidades a un objeto sin herencia.",
    "Un préstamo real suma <strong>cargos opcionales</strong> (seguro de vida, gastos). Con herencia: una subclase por plan × combinación.",
    cb(A, "model/Credito.java", method(A, "model/Credito.java", r"private CalculoDeCuota calculo")),
    "Cada cargo <strong>envuelve</strong> el cálculo y suma lo suyo; <code>Credito</code> arma la pila.",
    cb(D, "model/Credito.java", method(D, "model/Credito.java", r"private CalculoDeCuota calculo"))
    + cb(D, "model/plan/CargoSobreCuota.java", body(D, "model/plan/CargoSobreCuota.java"))
    + cb(D, "model/plan/SeguroDeVida.java", method(D, "model/plan/SeguroDeVida.java", r"protected BigDecimal cargo"))
    + cb(D, "model/plan/GastoDeAdministracion.java", method(D, "model/plan/GastoDeAdministracion.java", r"protected BigDecimal cargo"))
    + cb(D, "model/TipoCargo.java", body(D, "model/TipoCargo.java")),
    "Se otorgan o derogan <strong>por crédito</strong>. <strong>OCP</strong>, <strong>SRP</strong>.",
    "Simplificación: el seguro se calcula sobre el <strong>capital</strong>, no sobre el saldo.",
    ref="Commits <code>4eab370</code>, <code>54cd991</code>. Tabla nueva <code>credito_cargos</code>.",
    extra=roles([
        ("Component", "<code>CalculoDeCuota</code> (la Strategy de V3)"),
        ("ConcreteComponent", "<code>InteresSimple</code>, <code>SistemaFrances</code>"),
        ("Decorator", "<code>model/plan/CargoSobreCuota.java</code>"),
        ("ConcreteDecorator", "<code>SeguroDeVida</code> (0,1 %), <code>GastoDeAdministracion</code> ($500)"),
        ("Client", "<code>Credito.calculo()</code>"),
    ]) + trampa("¿No es otra Strategy?",
        "No: Strategy elige <strong>un</strong> algoritmo; los decoradores <strong>se apilan</strong> sobre él.")
    + figura("decorator-cargos", "Secuencia: cuota francesa con seguro y gastos"))

# ---------------- M13 Facade ----------------
evidencia = ('$ git grep -lE "AuthenticationManager|PasswordEncoder|TokenService|io\\.jsonwebtoken" \\\n'
             '    iteracion4 -- backend/src/main/java/com/uade/tpejemplo/controller\n'
             + (git("grep", "-lE", r"AuthenticationManager|PasswordEncoder|TokenService|io\.jsonwebtoken", D, "--", J + "controller").strip() or "(sin resultados)"))
m13 = bloque("facade", "M13. Facade: autenticación", "reconocida y nombrada",
    "<strong>Facade</strong> (GoF, estructural): interfaz simple sobre un componente de terceros complejo.",
    "El patrón <strong>ya existía sin nombre</strong>; <code>AuthController.actual</code> recibía un tipo de Spring.",
    cb(A, "controller/AuthController.java", method(A, "controller/AuthController.java", r"public ResponseEntity<AuthResponse> actual")),
    "Rol nombrado; el controlador recibe <code>UsuarioDetails</code>, como los demás.",
    cb(D, "service/AuthService.java", body(D, "service/AuthService.java"))
    + cb(D, "service/impl/AuthServiceImpl.java", lines(D, "service/impl/AuthServiceImpl.java", r"private final UsuarioRepository", 4), "", method(D, "service/impl/AuthServiceImpl.java", r"public AuthResponse login"))
    + '<p>Evidencia: ningún controlador usa el subsistema.</p><pre><code>' + e(evidencia) + '</code></pre>',
    "Esconde Spring Security y jjwt. <strong>Low Coupling</strong>, <strong>Protected Variations</strong>.",
    "Los controladores conservan solo anotaciones de borde (<code>@AuthenticationPrincipal</code>, <code>@PreAuthorize</code>).",
    ref="Commit <code>a0fafd3</code>.",
    extra=roles([
        ("Facade", "<code>service/AuthService.java</code>, <code>service/impl/AuthServiceImpl.java</code>"),
        ("Subsistema", "<code>AuthenticationManager</code>, <code>PasswordEncoder</code>, <code>TokenService</code> → <code>JwtUtil</code> (Adapter M7), <code>UsuarioRepository</code>"),
        ("Client", "<code>controller/AuthController.java</code>"),
    ]) + trampa("¿Entonces todos los services son Facade?",
        "No: los demás coordinan dominio propio (GRASP Controller); este oculta terceros.")
    + figura("facade-login", "Secuencia: login a través de la Facade"))

mejoras = [m11, m12, m13]

indice = [
 ("composite", "M11", "Saldo y mora por cuota, crédito y cartera", "<strong>Composite</strong>", "sumas repetidas"),
 ("decorator", "M12", "Seguro de vida y gastos sobre la cuota", "<strong>Decorator</strong>", "expansión del dominio"),
 ("facade", "M13", "Autenticación detrás de <code>AuthService</code>", "<strong>Facade</strong>", "patrón sin nombrar"),
 ("panel", "M14-M16", "Regla de mora, deuda del cliente, cargos en el alta", "Composite, Decorator", "panel"),
]
tabla_indice = "".join(f'<tr><td><a href="#{k}">{i}</a></td><td>{a}</td><td>{b}</td><td>{c}</td></tr>' for k, i, a, b, c in indice)

# ---------------- panel ----------------
panel = [
 ("M14", "Regla de mora", "Composite", "<code>CreditoServiceImpl.crear</code>: cliente con cuotas vencidas → 400."),
 ("M15", "Deuda del cliente", "Composite", "<code>GET /api/clientes/{dni}/deuda</code>; Clientes muestra saldo y vencido."),
 ("M16", "Cargos al otorgar", "Decorator", "Casillas en Créditos; la ficha lista los cargos."),
]
panel_html = '<table><thead><tr><th>ID</th><th>Funcionalidad</th><th>Usa</th><th>Dónde</th></tr></thead><tbody>' + "".join(f'<tr><td>{i}</td><td><strong>{f}</strong></td><td>{u}</td><td>{d}</td></tr>' for i, f, u, d in panel) + '</tbody></table>'
panel_codigo = (cb(D, "service/impl/CreditoServiceImpl.java", lines(D, "service/impl/CreditoServiceImpl.java", r"// Composite: la cartera del cliente", 6))
    + cb(D, "service/impl/ClienteServiceImpl.java", method(D, "service/impl/ClienteServiceImpl.java", r"public DeudaClienteResponse deuda")))

# ---------------- revisión ----------------
rev = [
 ("BS14", "a989edd", "DNI <code>\"sdfg\"</code> aceptado.", "Solo 7 u 8 dígitos (<code>@Pattern</code>)."),
 ("BS11b", "7b47ccc", "<code>Credito.saldo</code> repetía la regla del anulado.", "La sabe la cuota; <code>sumar(Function)</code>."),
 ("DER", "b6ad026", "Sin <code>credito_cargos</code>.", "<code>der-v4</code>: PK compuesta, <code>rol</code> enum."),
]
rev_html = '<table><thead><tr><th>ID · commit</th><th>Problema</th><th>Cambio</th></tr></thead><tbody>' + "".join(f'<tr><td><strong>{i}</strong><br><span class="ref"><code>{c}</code></span></td><td>{p}</td><td>{s}</td></tr>' for i, c, p, s in rev) + '</tbody></table>'

convenciones = """<p>Convención: <strong><code>I&lt;Clase&gt;</code></strong> por clase de modelo y <strong><code>XService</code> + <code>XServiceImpl</code></strong>. Excepciones:</p>
<table><thead><tr><th>Clase</th><th>Excepción</th><th>Motivo</th></tr></thead><tbody>
<tr><td><code>model/interfaces/Deuda.java</code>, <code>CalculoDeCuota.java</code></td><td>sin prefijo <code>I</code></td><td>Nombran un <strong>rol de patrón</strong>.</td></tr>
<tr><td><code>model/Cartera.java</code></td><td>sin <code>ICartera</code></td><td>No es entidad; su interfaz es <code>Deuda</code>.</td></tr>
<tr><td><code>model/TipoPlan.java</code>, <code>model/TipoCargo.java</code></td><td>sin <code>I</code></td><td>Enums <strong>registro</strong> de estrategias y decoradores.</td></tr>
<tr><td><code>model/EstadoCredito.java</code></td><td>sin <code>IEstadoCredito</code></td><td>Enum sin comportamiento.</td></tr>
<tr><td><code>service/TokenService.java</code> → <code>security/JwtUtil.java</code></td><td>no es <code>TokenServiceImpl</code></td><td>Adapter: infraestructura.</td></tr>
</tbody></table>"""

matriz = [
 ("Information Expert", "<code>Cuota</code> sabe su saldo y su mora; <code>Credito</code>, su estado"),
 ("Creator", "<code>Credito</code> crea sus cuotas; <code>TipoCargo</code>, su decorador"),
 ("Controller", "<code>controller/*Controller</code> delegan en <code>XService</code>"),
 ("Polymorphism", "<code>Deuda</code> y <code>CalculoDeCuota</code>, sin <code>if</code> por tipo"),
 ("Low Coupling / Indirection", "<code>AuthService</code> (Facade), <code>TokenService</code> (Adapter)"),
 ("Protected Variations", "fórmula, cargos, librería JWT"),
 ("SRP / OCP", "un cargo o una fórmula por clase; agregar uno no toca <code>Credito</code>"),
 ("LSP / ISP", "cualquier <code>Deuda</code> sirve donde se pide una; <code>Deuda</code> sin <code>agregar/quitar</code>"),
 ("DIP", "<code>Credito</code> → <code>CalculoDeCuota</code>; controladores → interfaces de servicio"),
]
matriz_html = "<table><thead><tr><th>Principio</th><th>Dónde</th></tr></thead><tbody>" + "".join(f"<tr><td><strong>{a}</strong></td><td>{b}</td></tr>" for a, b in matriz) + "</tbody></table>"

# ---------------- timeline ----------------
log_v4 = git("log", "--oneline", "--no-merges", f"{A}..{D}").strip()
n_v4 = len(log_v4.split("\n"))

timeline = f'''<div class="timeline">
<div class="hito"><div class="punto"></div><h4>V0</h4><p class="tema">Proyecto inicial</p><p class="meta"><code>main@2a90af8</code></p>
<ul><li>CRUD de clientes, créditos y cobranzas</li><li>Cuota cargada a mano</li></ul></div>
<div class="hito"><div class="punto"></div><h4>V1</h4><p class="tema">Bad smells</p><p class="meta">08/09 · <code>86c75cc</code></p>
<ul><li>13 smells</li><li><code>UsuarioDetails</code> como Adapter</li></ul></div>
<div class="hito"><div class="punto"></div><h4>V2</h4><p class="tema">GRASP + interfaces</p><p class="meta">15/09 · tag <code>v2</code></p>
<ul><li>Expert y Creator</li><li>Restituye las interfaces de servicio</li></ul></div>
<div class="hito"><div class="punto"></div><h4>V3</h4><p class="tema">MVC + Strategy + Adapter</p><p class="meta">13/10 · tag <code>v3</code></p>
<ul><li>Cuota simple o francesa</li><li><code>TokenService</code> sobre jjwt</li><li>43 tests</li></ul></div>
<div class="hito actual"><div class="punto"></div><h4>V4</h4><p class="tema">Composite + Decorator + Facade</p><p class="meta">27/10 · {n_v4} commits</p>
<ul><li><strong>Composite</strong>: <code>Deuda</code> (M11)</li><li><strong>Decorator</strong>: cargos (M12)</li><li><strong>Facade</strong>: <code>AuthService</code> (M13)</li><li>Panel: deuda del cliente y cargos</li><li>63 tests</li></ul></div>
</div>
<details><summary>Commits V3 → V4 (git log v3..iteracion4)</summary><pre><code>{e(log_v4)}</code></pre></details>'''

mapeo = [
 ("model/*, model/interfaces/*, model/plan/*", "Modelo", "Reglas; Composite, Strategy y Decorator."),
 ("repository/*", "Modelo", "Acceso a datos."),
 ("service/*, service/impl/*", "Modelo", "Casos de uso y transacción; <code>AuthService</code> es Facade."),
 ("controller/*, dto/*", "Controlador", "HTTP ↔ modelo por DTO."),
 ("exception/GlobalExceptionHandler", "Controlador", "Excepción → código HTTP."),
 ("security/*, config/*", "Controlador", "401/403 antes del controlador."),
 ("Frontend pages/*, components/*, api/*", "Vista", "Presenta lo que decide el modelo."),
]
rows = "\n".join(f"<tr><td><code>{a}</code></td><td><strong>{b}</strong></td><td>{c}</td></tr>" for a, b, c in mapeo)

tecnologias = '''<dl class="tec">
<dt>Spring Boot, Spring Security</dt><dd>Backend REST; autenticación y autorización (subsistema de la Facade).</dd>
<dt>JWT y jjwt</dt><dd>Token de sesión; jjwt es el Adaptee de M7.</dd>
<dt>JPA / Hibernate, H2</dt><dd>Mapeo objeto-relacional sobre base en memoria.</dd>
<dt>React, Redux, Vite</dt><dd>Vista, estado del cliente, build.</dd>
<dt>JUnit 5, AssertJ, Mockito</dt><dd>Tests de dominio, HTTP e integración.</dd>
<dt>PlantUML, archify</dt><dd>Diagramas de clases y de secuencia desde texto.</dd>
</dl>'''

der_svg = re.sub(r"<\?xml[^>]*\?>", "", open(os.path.join(REPO, "docs/diagramas/der-v4.svg")).read())
der_svg = re.sub(r'(<svg[^>]*?) style="[^"]*" width="[^"]*" height="[^"]*"', r'\1 style="width:100%;height:auto"', der_svg, count=1)


def clases(vista):
    return re.sub(r"<\?xml[^>]*\?>", "", open(os.path.join(REPO, f"docs/diagramas/clases-v4-{vista}.svg")).read())


pendientes = '''<ul>
<li><strong>H8</strong>: quién cobró y quién anuló.</li>
<li><strong>P-M9</strong>: tasa con unidad por plan; cuota con capital e interés separados (habilita IVA sobre intereses).</li>
<li><strong>O3b</strong>: ajustar la última cuota para que el total cierre exacto.</li>
<li><strong>O8</strong>: pagos parciales.</li>
<li><strong>Base persistente</strong>: prod sigue en H2 en memoria.</li>
<li><strong>Seguro sobre saldo</strong>: hoy sobre capital (M12).</li>
</ul>'''

LIGHTBOX = """<div class="lightbox" id="lb" role="dialog" aria-modal="true" aria-label="Diagrama ampliado">
<div class="lb-bar"><span class="lb-title" id="lbTitle"></span>
<button class="sec" id="lbOut" aria-label="Alejar">−</button><span class="lb-zoom" id="lbZoom">100%</span><button class="sec" id="lbIn" aria-label="Acercar">+</button>
<button class="sec" id="lbFit">Ajustar</button><button id="lbClose" aria-label="Cerrar">✕</button></div>
<div class="lb-scroll" id="lbScroll"><div class="lb-canvas" id="lbCanvas"></div></div></div>
<script>
(function () {
  var lb = document.getElementById("lb"), canvas = document.getElementById("lbCanvas"), sc = document.getElementById("lbScroll");
  var label = document.getElementById("lbZoom"), w = 800, h = 600, k = 1;
  function size(svg) {
    var vb = (svg.getAttribute("viewBox") || "").trim().split(/[\\s,]+/).map(Number);
    if (vb.length === 4 && vb[2] > 0) return [vb[2], vb[3]];
    var r = svg.getBoundingClientRect(); return [r.width || 800, r.height || 600];
  }
  function apply() { canvas.style.width = Math.round(w * k) + "px"; canvas.style.height = Math.round(h * k) + "px"; label.textContent = Math.round(k * 100) + "%"; }
  function fit() { k = Math.min((sc.clientWidth - 60) / w, (sc.clientHeight - 60) / h, 1.5); apply(); sc.scrollLeft = sc.scrollTop = 0; }
  function zoom(f, cx, cy) {
    var r = sc.getBoundingClientRect(), ax = cx == null ? sc.clientWidth / 2 : cx - r.left, ay = cy == null ? sc.clientHeight / 2 : cy - r.top;
    var x = sc.scrollLeft + ax, y = sc.scrollTop + ay, p = k;
    k = Math.min(Math.max(k * f, 0.1), 8); apply();
    sc.scrollLeft = x * k / p - ax; sc.scrollTop = y * k / p - ay;
  }
  function open(svg, title) {
    var c = svg.cloneNode(true); c.removeAttribute("style"); c.removeAttribute("width"); c.removeAttribute("height");
    var d = size(svg); w = d[0]; h = d[1];
    canvas.innerHTML = ""; canvas.appendChild(c);
    document.getElementById("lbTitle").textContent = title;
    lb.classList.add("open"); document.body.classList.add("lb-locked"); fit();
  }
  function close() { lb.classList.remove("open"); document.body.classList.remove("lb-locked"); canvas.innerHTML = ""; }
  function titulo(box) {
    var fig = box.closest("figure"), cap = fig && fig.querySelector("figcaption");
    var art = box.closest("article"), h3 = art && art.querySelector("h3");
    var pre = h3 ? h3.childNodes[0].textContent.trim() + " · " : "";
    if (cap) return pre + cap.textContent.trim();
    for (var n = box.previousElementSibling; n; n = n.previousElementSibling) if (/^H[2-4]$/.test(n.tagName)) return n.textContent.trim();
    return h3 ? h3.childNodes[0].textContent.trim() : "Diagrama";
  }
  document.querySelectorAll(".svgbox, .mvcsvg, .clases").forEach(function (box) {
    var svg = box.querySelector("svg"); if (!svg) return;
    box.classList.add("zoomable");
    box.addEventListener("click", function () { open(svg, titulo(box)); });
  });
  document.getElementById("lbClose").onclick = close;
  document.getElementById("lbIn").onclick = function () { zoom(1.3); };
  document.getElementById("lbOut").onclick = function () { zoom(1 / 1.3); };
  document.getElementById("lbFit").onclick = fit;
  lb.addEventListener("click", function (e) { if (e.target === lb || e.target === sc) close(); });
  document.addEventListener("keydown", function (e) {
    if (!lb.classList.contains("open")) return;
    if (e.key === "Escape") close();
    else if (e.key === "+" || e.key === "=") zoom(1.3);
    else if (e.key === "-") zoom(1 / 1.3);
    else if (e.key === "0") fit();
  });
  sc.addEventListener("wheel", function (e) { e.preventDefault(); zoom(e.deltaY < 0 ? 1.12 : 1 / 1.12, e.clientX, e.clientY); }, { passive: false });
  var drag = false, sx, sy, l0, t0, moved = false;
  sc.addEventListener("mousedown", function (e) { drag = true; moved = false; sx = e.clientX; sy = e.clientY; l0 = sc.scrollLeft; t0 = sc.scrollTop; sc.classList.add("grabbing"); });
  window.addEventListener("mousemove", function (e) { if (!drag) return; moved = true; sc.scrollLeft = l0 - (e.clientX - sx); sc.scrollTop = t0 - (e.clientY - sy); });
  window.addEventListener("mouseup", function () { drag = false; sc.classList.remove("grabbing"); });
})();
</script>"""


doc = f'''<!doctype html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Reporte V4 — TPO Grupo 7</title><style>{CSS}</style></head>
<body>
<div id="barra"><strong style="color:var(--navy)">Reporte V4</strong>
<nav><a href="#portada">Portada</a><a href="#timeline">Timeline</a><a href="#tecnologias">Tecnologías</a><a href="#mvc">MVC</a><a href="#mejoras">Mejoras</a><a href="#composite">Composite</a><a href="#decorator">Decorator</a><a href="#facade">Facade</a><a href="#panel">Panel</a><a href="#convenciones">Convenciones</a><a href="#matriz">GRASP/SOLID</a><a href="#revision">Revisión</a><a href="#verificacion">Verificación</a><a href="#casos-de-uso">Casos de uso</a><a href="#clases">Diagramas</a><a href="#pendientes">Pendientes</a></nav>
<button class="sec" onclick="document.querySelectorAll('details.detalle').forEach(d=>d.open=true);window.print()">Exportar PDF</button></div>
<main>

<section id="portada" class="portada">
<div class="marca">UADE</div>
<h1>TPO Grupo 7<br>Dashboard de préstamos</h1><div class="rule"></div>
<div class="sub">Iteración 4: Composite + Decorator + Facade</div>
<div class="sub">Proceso de Desarrollo de Software · 27/10/2026</div>
<ul><li><strong>Integrantes:</strong> Andrei, Regulo</li>
<li><strong>Código:</strong> “antes” = tag <code>v3</code>; “después” = rama <code>iteracion4</code>.</li>
<li><strong>Rutas:</strong> relativas a <code>backend/src/main/java/com/uade/tpejemplo/</code>.</li></ul>
</section>

<section id="timeline"><h2>Timeline V0 → V4</h2>
{timeline}</section>

<section id="tecnologias"><h2>Tecnologías</h2>
{tecnologias}</section>

<section id="mvc"><h2>MVC en Spring Boot</h2>
<div class="clases">{archify_svg("mvc")}</div>
<table><thead><tr><th>Clase / paquete</th><th>Componente</th><th>Justificación</th></tr></thead><tbody>{rows}</tbody></table>
<p>Nuevo en V4: <code>GET /api/clientes/{{dni}}/deuda</code> (M15).</p>
</section>

<section id="mejoras"><h2>Mejoras al dominio</h2>
<p>Formato: <strong>Patrón → Problema (<code>v3</code>) → Solución (<code>iteracion4</code>) → Por qué → Consecuencias</strong>.</p>
<table><thead><tr><th>ID</th><th>Mejora</th><th>Patrón</th><th>Hueco</th></tr></thead><tbody>{tabla_indice}</tbody></table>
{"".join(mejoras)}
</section>

<section id="panel"><h2>Funcionalidades nuevas del panel</h2>
{panel_html}
{panel_codigo}
</section>

<section id="convenciones"><h2>Convenciones de la cátedra</h2>
{convenciones}
</section>

<section id="matriz"><h2>GRASP y SOLID</h2>
{matriz_html}
</section>

<section id="revision"><h2>Mejoras de la revisión</h2>
{rev_html}
</section>

<section id="verificacion"><h2>Verificación</h2>
<ul>
<li><strong>63 tests, 0 fallas</strong> (<code>mvn test</code>; V3: 43): 29 de dominio, 14 de cálculo de cuota, 11 HTTP, 8 de integración, <code>contextLoads</code>.</li>
<li><strong>Casos de uso por API</strong>: <code>docs/casos-de-uso/verificar-cu.sh</code>, incluida la mora (400).</li>
<li><strong>Hallazgo H5, corregido</strong>: <code>verificar-cu.sh</code> detectó 500 al consultar créditos (<code>Credito.cargos</code> lazy fuera de la transacción); fix: <code>CreditoResponse.desde</code> copia los cargos (<code>6286109</code>), test <code>CreditoConCargosTest</code>.</li>
</ul>
</section>

<section id="casos-de-uso"><h2>Casos de uso</h2>
<p>Fichas y trazabilidad: <a href="../casos-de-uso/README.md"><code>docs/casos-de-uso/README.md</code></a>. Nuevo: UC21; cambia UC07.</p>
<p><a href="../casos-de-uso/casos-de-uso-v4.svg"><img src="../casos-de-uso/casos-de-uso-v4.svg" alt="Diagrama de casos de uso V4" style="width:100%;background:#fff;border:1px solid var(--linea);border-radius:4px"></a></p>
</section>

<section id="clases"><h2>Diagramas de clases y DER</h2>
<p>Fuente: <code>docs/diagramas/clases-v4.puml</code> y <code>der-v4.py</code>.</p>
<h3>Capas MVC</h3>
<div class="clases">{clases("mvc")}</div>
<h3>Seguridad: Facade y Adapter</h3>
<div class="clases">{clases("seguridad")}</div>
<h3>Modelo: Composite, Strategy y Decorator</h3>
<div class="clases">{clases("modelo")}</div>
<h3>DER</h3>
<div class="clases">{der_svg}</div>
</section>

<section id="pendientes"><h2>Cambios pendientes</h2>
{pendientes}
<p>Lista completa: <a href="../entrega/lista-mejoras.md"><code>docs/entrega/lista-mejoras.md</code></a>.</p>
</section>
</main>
{LIGHTBOX}
</body></html>'''
open(OUT + "/reporte-v4.html", "w").write(doc)
print(len(doc))
