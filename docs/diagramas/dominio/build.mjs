// Diagrama de clases del dominio: lee el bloque clases-v4-modelo de clases-v4.puml,
// calcula la disposicion con ELK (lineas ortogonales) y dibuja el SVG con el
// estilo de diagram-design. Uso: npm install && node build.mjs
import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import ELK from "elkjs/lib/elk.bundled.js";

const AQUI = dirname(fileURLToPath(import.meta.url));
const PUML = join(AQUI, "..", "clases-v4.puml");
const SALIDA = join(AQUI, "..", "clases-v4-dominio.svg");

const C = {
  paper: "#f5f5f5", paper2: "#ececec", ink: "#2d3142", muted: "#4f5d75",
  soft: "#5a6580", rule: "#bfc0c0", box: "#ffffff",
};
// Un color por paquete (paleta series de diagram-design) y uno por familia de relacion
const PAQUETE = {
  "model.credito": "#5e7a9b", "model.plan": "#7c8f6f", "model.usuario": "#9c6b50",
  "model.cliente": "#b8915a", "service.impl": "#6e6479",
};
const FAMILIA = {
  herencia: { color: "#2d3142", nombre: "implementa / extiende" },
  estructura: { color: "#2e5aa8", nombre: "composición / agregación / asociación" },
  uso: { color: "#8b93a7", nombre: "dependencia" },
};
// Patrones GoF: rol de cada clase (pastilla en la caja) y ficha del boton de vista
const PATRONES = {
  Composite: {
    color: "#c2410c",
    roles: { Deuda: "Component", ICredito: "Component", ICuota: "Component", Credito: "Composite", Cartera: "Composite", Cuota: "Leaf", CreditoServiceImpl: "Client" },
    problema: "Saldo y mora se consultan igual en una cuota, un crédito o una cartera: la hoja responde y el compuesto suma a sus hijos.",
  },
  Strategy: {
    color: "#0f766e",
    roles: { CondicionesDeCredito: "Context", CalculoDeCuota: "Strategy", InteresSimple: "ConcreteStrategy", SistemaFrances: "ConcreteStrategy", TipoPlan: "Selector" },
    problema: "El sistema de amortización se elige al otorgar el crédito y se intercambia sin tocar a quien calcula la cuota.",
  },
  Decorator: {
    color: "#7c3aed",
    roles: { CalculoDeCuota: "Component", InteresSimple: "ConcreteComponent", SistemaFrances: "ConcreteComponent", CargoSobreCuota: "Decorator", SeguroDeVida: "ConcreteDecorator", GastoDeAdministracion: "ConcreteDecorator", TipoCargo: "Ensambla", CondicionesDeCredito: "Client" },
    problema: "Los cargos opcionales se apilan sobre el cálculo base: cada capa delega en la envuelta y suma su cargo, sin una subclase por combinación.",
  },
};
const rolesDe = (id) => Object.entries(PATRONES).filter(([, p]) => p.roles[id]).map(([n, p]) => ({ texto: `${n.toUpperCase()} · ${p.roles[id]}`, color: p.color }));
const anchoPastilla = (t) => Math.ceil(t.length * 5.4) + 12;
const ROLES_GOF = new Set(["Component", "Composite", "Leaf", "Strategy", "ConcreteStrategy", "ConcreteComponent", "Decorator", "ConcreteDecorator", "Context"]);
const MONO = "'Geist Mono', ui-monospace, Menlo, monospace";
const SANS = "'Geist', -apple-system, 'Helvetica Neue', sans-serif";
const SERIF = "'Instrument Serif', Georgia, serif";
const MIEMBRO = { size: 10, ancho: 6.1, alto: 14 };
const NOMBRE = { size: 12.5, ancho: 7.6, alto: 17 };
const ESTEREO = { size: 8.5, ancho: 5.3, alto: 12 };
const ETIQUETA = { size: 9.5, ancho: 5.8, alto: 13 };
const PAD = 8;

// ---------- lectura del puml ----------
function leerModelo() {
  const texto = readFileSync(PUML, "utf8");
  const ini = texto.indexOf("@startuml clases-v4-modelo");
  const fin = texto.indexOf("@enduml", ini);
  const lineas = texto.slice(ini, fin).split("\n").map((l) => l.trim());
  const paquetes = [], clases = new Map(), relaciones = [];
  let paquete = null, clase = null;
  for (const l of lineas) {
    let m;
    if (clase) {
      if (l === "}") { clase = null; continue; }
      if (l) clase.miembros.push(l);
      continue;
    }
    if ((m = l.match(/^package "([^"]+)" \{$/))) {
      paquete = { id: m[1], clases: [] };
      paquetes.push(paquete);
    } else if (l === "}" && paquete) {
      paquete = null;
    } else if ((m = l.match(/^(abstract class|class|interface|enum) (\w+)(?: <<(.+?)>>)? \{$/))) {
      clase = { id: m[2], tipo: m[1], estereotipo: m[3] || null, miembros: [], paquete: paquete.id };
      clases.set(clase.id, clase);
      paquete.clases.push(clase.id);
    } else if ((m = l.match(/^(\w+)\s+(?:"([^"]+)"\s+)?(<\|\.\.|<\|--|o-->|\*-->|\*--|-->|\.\.>)\s+(?:"([^"]+)"\s+)?(\w+)\s*:\s*(.+?)\s*>?$/))) {
      relaciones.push({ a: m[1], multA: m[2], flecha: m[3], multB: m[4], b: m[5], verbo: m[6] });
    }
  }
  return { paquetes, clases, relaciones };
}

// ---------- medidas de cada caja ----------
function cabecera(c) {
  const linea = [];
  if (c.tipo === "interface") linea.push("interface");
  if (c.tipo === "enum") linea.push("enumeration");
  if (c.tipo === "abstract class") linea.push("abstract");
  if (c.estereotipo) linea.push(...c.estereotipo.split(" / ").filter((t) => !ROLES_GOF.has(t)));
  return linea.length ? "«" + linea.join(" · ") + "»" : null;
}

function compartimentos(c) {
  const esMetodo = (l) => l.includes("(");
  if (c.tipo === "enum") {
    const valores = c.miembros.filter((l) => /^[A-Z_]+$/.test(l));
    const resto = c.miembros.filter((l) => !/^[A-Z_]+$/.test(l));
    return [valores, resto.filter((l) => !esMetodo(l)), resto.filter(esMetodo)].filter((g) => g.length);
  }
  return [c.miembros.filter((l) => !esMetodo(l)), c.miembros.filter(esMetodo)].filter((g) => g.length);
}

function medir(c) {
  const est = cabecera(c);
  const grupos = compartimentos(c);
  const textos = grupos.flat().map(limpiar);
  const pastillas = rolesDe(c.id);
  const anchoPast = pastillas.reduce((s, p) => s + anchoPastilla(p.texto) + 4, 0);
  const ancho = Math.max(
    c.id.length * NOMBRE.ancho,
    est ? est.length * ESTEREO.ancho : 0,
    ...textos.map((t) => t.length * MIEMBRO.ancho),
    anchoPast,
  ) + PAD * 2;
  const altoCab = PAD + (pastillas.length ? 18 : 0) + (est ? ESTEREO.alto : 0) + NOMBRE.alto + PAD - 2;
  const alto = altoCab + grupos.reduce((s, g) => s + g.length * MIEMBRO.alto + PAD, 0);
  return { ancho: Math.ceil(ancho), alto: Math.ceil(alto), altoCab, est, grupos, pastillas };
}

const limpiar = (l) => l.replace(/\{(static|abstract)\}\s*/g, "");

// ---------- tipos de relacion ----------
const TIPOS = {
  "<|..": { familia: "herencia", trazo: "5,4", inicio: "triangulo", fin: null, verbo: true },
  "<|--": { familia: "herencia", trazo: null, inicio: "triangulo", fin: null, verbo: true },
  "o-->": { familia: "estructura", trazo: null, inicio: "rombo-vacio", fin: "punta" },
  "*-->": { familia: "estructura", trazo: null, inicio: "rombo-lleno", fin: "punta" },
  "*--": { familia: "estructura", trazo: null, inicio: "rombo-lleno", fin: null },
  "-->": { familia: "estructura", trazo: null, inicio: null, fin: "punta" },
  "..>": { familia: "uso", trazo: "4,3", inicio: null, fin: "punta" },
};

// ---------- layout ----------
async function disponer(modelo, medidas) {
  const etiqueta = (texto, lugar) => ({
    text: texto,
    width: Math.ceil(texto.length * ETIQUETA.ancho) + 8,
    height: ETIQUETA.alto,
    layoutOptions: { "elk.edgeLabels.placement": lugar },
  });
  const grafo = {
    id: "raiz",
    layoutOptions: {
      "elk.algorithm": "layered",
      "elk.direction": "DOWN",
      "elk.edgeRouting": "ORTHOGONAL",
      "elk.hierarchyHandling": "INCLUDE_CHILDREN",
      "elk.spacing.nodeNode": "48",
      "elk.layered.spacing.nodeNodeBetweenLayers": "64",
      "elk.spacing.edgeNode": "22",
      "elk.spacing.edgeEdge": "14",
      "elk.layered.spacing.edgeNodeBetweenLayers": "22",
      "elk.layered.spacing.edgeEdgeBetweenLayers": "14",
      "elk.spacing.edgeLabel": "4",
      "elk.layered.nodePlacement.strategy": "NETWORK_SIMPLEX",
      "elk.layered.crossingMinimization.strategy": "LAYER_SWEEP",
      "elk.layered.thoroughness": "40",
      "elk.layered.mergeEdges": "false",
    },
    children: modelo.paquetes.map((p) => ({
      id: "pkg:" + p.id,
      layoutOptions: { "elk.padding": "[top=40,left=24,bottom=24,right=24]" },
      children: p.clases.map((id) => ({ id, width: medidas.get(id).ancho, height: medidas.get(id).alto })),
    })),
    edges: modelo.relaciones.map((r, i) => {
      const labels = [etiqueta(r.verbo, "CENTER")];
      if (r.multA) labels.push(etiqueta(r.multA, "TAIL"));
      if (r.multB) labels.push(etiqueta(r.multB, "HEAD"));
      return { id: "e" + i, sources: [r.a], targets: [r.b], labels };
    }),
  };
  return new ELK().layout(grafo);
}

// ---------- dibujo ----------
const esc = (t) => t.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

function caja(c, m, x, y) {
  const col = PAQUETE[c.paquete];
  const s = [`<g class="clase" data-id="${c.id}">`];
  s.push(`<rect x="${x}" y="${y}" width="${m.ancho}" height="${m.alto}" rx="6" fill="${C.box}" stroke="${col}" stroke-width="1.2"/>`);
  s.push(`<path d="M${x + 0.6},${y + m.altoCab} V${y + 6} Q${x + 0.6},${y + 0.6} ${x + 6},${y + 0.6} H${x + m.ancho - 6} Q${x + m.ancho - 0.6},${y + 0.6} ${x + m.ancho - 0.6},${y + 6} V${y + m.altoCab} Z" fill="${col}" fill-opacity="0.32"/>`);
  let cy = y + PAD;
  const cx = x + m.ancho / 2;
  if (m.pastillas.length) {
    let px = x + PAD;
    for (const p of m.pastillas) {
      const w = anchoPastilla(p.texto);
      s.push(`<rect x="${px}" y="${y + 6}" width="${w}" height="15" rx="7.5" fill="${p.color}"/>`);
      s.push(`<text x="${px + w / 2}" y="${y + 16.5}" text-anchor="middle" font-family="${MONO}" font-size="8.5" font-weight="600" fill="#ffffff" letter-spacing="0.3">${esc(p.texto)}</text>`);
      px += w + 4;
    }
    cy += 18;
  }
  if (m.est) {
    cy += ESTEREO.alto - 3;
    s.push(`<text x="${cx}" y="${cy}" text-anchor="middle" font-family="${MONO}" font-size="${ESTEREO.size}" fill="${C.soft}" letter-spacing="0.4">${esc(m.est)}</text>`);
    cy += 3;
  }
  cy += NOMBRE.alto - 4;
  const italica = c.tipo === "abstract class" || c.tipo === "interface" ? ` font-style="italic"` : "";
  s.push(`<text x="${cx}" y="${cy}" text-anchor="middle" font-family="${SANS}" font-size="${NOMBRE.size}" font-weight="600" fill="${C.ink}"${italica}>${esc(c.id)}</text>`);
  cy = y + m.altoCab;
  for (const g of m.grupos) {
    s.push(`<line x1="${x}" y1="${cy}" x2="${x + m.ancho}" y2="${cy}" stroke="${C.ink}" stroke-width="0.6" stroke-opacity="0.45"/>`);
    cy += PAD / 2;
    for (const l of g) {
      cy += MIEMBRO.alto;
      const deco = [];
      if (l.includes("{static}")) deco.push(`text-decoration="underline"`);
      if (l.includes("{abstract}")) deco.push(`font-style="italic"`);
      s.push(`<text x="${x + PAD}" y="${cy - 3.5}" font-family="${MONO}" font-size="${MIEMBRO.size}" fill="${C.ink}" ${deco.join(" ")}>${esc(limpiar(l))}</text>`);
    }
    cy += PAD / 2;
  }
  s.push("</g>");
  return s.join("\n");
}

// Polilinea ortogonal con esquinas redondeadas
function trazo(p, r = 7) {
  let d = `M${p[0].x},${p[0].y}`;
  for (let i = 1; i < p.length - 1; i++) {
    const a = p[i - 1], b = p[i], c = p[i + 1];
    const d1 = Math.hypot(b.x - a.x, b.y - a.y), d2 = Math.hypot(c.x - b.x, c.y - b.y);
    const k = Math.min(r, d1 / 2, d2 / 2);
    const p1 = { x: b.x + ((a.x - b.x) / d1) * k, y: b.y + ((a.y - b.y) / d1) * k };
    const p2 = { x: b.x + ((c.x - b.x) / d2) * k, y: b.y + ((c.y - b.y) / d2) * k };
    d += ` L${p1.x},${p1.y} Q${b.x},${b.y} ${p2.x},${p2.y}`;
  }
  const u = p[p.length - 1];
  return d + ` L${u.x},${u.y}`;
}

// Marca en el extremo `punta`, apuntando desde `previo` hacia `punta`
function marca(tipo, punta, previo, color) {
  const L = Math.hypot(punta.x - previo.x, punta.y - previo.y) || 1;
  const ux = (punta.x - previo.x) / L, uy = (punta.y - previo.y) / L;
  const nx = -uy, ny = ux;
  const pt = (a, b) => `${punta.x - ux * a + nx * b},${punta.y - uy * a + ny * b}`;
  if (tipo === "triangulo") {
    return `<polygon points="${pt(0, 0)} ${pt(13, 7)} ${pt(13, -7)}" fill="${C.box}" stroke="${color}" stroke-width="1.2" stroke-linejoin="miter"/>`;
  }
  if (tipo === "rombo-lleno" || tipo === "rombo-vacio") {
    const relleno = tipo === "rombo-lleno" ? color : C.box;
    return `<polygon points="${pt(0, 0)} ${pt(8, 5)} ${pt(16, 0)} ${pt(8, -5)}" fill="${relleno}" stroke="${color}" stroke-width="1.2"/>`;
  }
  if (tipo === "punta") {
    return `<polyline points="${pt(10, 5)} ${pt(0, 0)} ${pt(10, -5)}" fill="none" stroke="${color}" stroke-width="1.3" stroke-linejoin="miter"/>`;
  }
  return "";
}

function dibujar(modelo, medidas, g) {
  const abs = new Map();
  const recorrer = (n, ox, oy) => {
    const x = ox + (n.x || 0), y = oy + (n.y || 0);
    abs.set(n.id, { x, y, w: n.width, h: n.height });
    (n.children || []).forEach((h) => recorrer(h, x, y));
  };
  recorrer(g, 0, 0);

  const MARGEN = 32, TITULO = 100;
  const W = Math.ceil(g.width) + MARGEN * 2, H = Math.ceil(g.height) + MARGEN * 2 + TITULO;
  const out = [];
  out.push(`<svg xmlns="http://www.w3.org/2000/svg" data-dominio="" viewBox="0 0 ${W} ${H}" width="${W}" height="${H}" font-family="${SANS}">`);
  out.push(`<style>@import url('https://fonts.googleapis.com/css2?family=Geist:wght@400;600&amp;family=Geist+Mono:wght@400&amp;family=Instrument+Serif:ital@0;1&amp;display=swap');</style>`);
  out.push(`<rect width="${W}" height="${H}" fill="${C.paper}"/>`);
  out.push(`<text x="${MARGEN}" y="${MARGEN + 22}" font-family="${SERIF}" font-size="30" fill="${C.ink}">Diagrama de clases del dominio</text>`);
  out.push(`<text x="${MARGEN}" y="${MARGEN + 42}" font-family="${MONO}" font-size="10" fill="${C.soft}" letter-spacing="1">DASHBOARD DE PRÉSTAMOS · V4 · CADA RELACIÓN SE LEE COMO ORACIÓN, DEL ORIGEN AL DESTINO</text>`);
  let lx = W - MARGEN - 760;
  for (const f of Object.values(FAMILIA)) {
    out.push(`<line x1="${lx}" y1="${MARGEN + 18}" x2="${lx + 28}" y2="${MARGEN + 18}" stroke="${f.color}" stroke-width="2"${f === FAMILIA.uso ? ` stroke-dasharray="4,3"` : ""}/>`);
    out.push(`<text x="${lx + 36}" y="${MARGEN + 22}" font-family="${MONO}" font-size="11" fill="${C.ink}">${esc(f.nombre)}</text>`);
    lx += 64 + f.nombre.length * 6.6;
  }
  let bx = MARGEN;
  out.push(`<text x="${bx}" y="${MARGEN + 77}" font-family="${MONO}" font-size="12" fill="${C.soft}" letter-spacing="1">VER PATRÓN</text>`);
  bx += 104;
  for (const [nombre, p] of Object.entries(PATRONES)) {
    const w = nombre.length * 11 + 40;
    const roles = {};
    for (const [clase, rol] of Object.entries(p.roles)) (roles[rol] = roles[rol] || []).push(clase);
    const ficha = `<div style='font:600 14px Geist,sans-serif;color:${p.color}'>${nombre}</div>` +
      `<div style='margin:4px 0 6px'>${p.problema}</div>` +
      Object.entries(roles).map(([rol, cs]) => `<div><b>${rol}</b>: ${cs.join(", ")}</div>`).join("");
    out.push(`<g class="btn-patron" data-patron="${nombre}" data-color="${p.color}" data-clases="${Object.keys(p.roles).join(",")}" data-ficha="${esc(ficha).replace(/"/g, "&quot;")}">` +
      `<rect x="${bx}" y="${MARGEN + 56}" width="${w}" height="32" rx="16" fill="${p.color}"/>` +
      `<text x="${bx + w / 2}" y="${MARGEN + 77.5}" text-anchor="middle" font-family="${SANS}" font-size="16" font-weight="600" fill="#ffffff">${nombre}</text></g>`);
    bx += w + 12;
  }
  out.push(`<g transform="translate(${MARGEN},${MARGEN + TITULO})">`);

  // paquetes
  for (const p of g.children) {
    const a = abs.get(p.id);
    out.push(`<rect x="${a.x}" y="${a.y}" width="${a.w}" height="${a.h}" rx="8" fill="${PAQUETE[p.id.slice(4)]}" fill-opacity="0.11" stroke="${PAQUETE[p.id.slice(4)]}" stroke-opacity="0.55" stroke-width="1.2"/>`);
    out.push(`<text x="${a.x + 16}" y="${a.y + 24}" font-family="${MONO}" font-size="11" font-weight="600" fill="${PAQUETE[p.id.slice(4)]}" letter-spacing="1.2">${esc(p.id.slice(4).toUpperCase())}</text>`);
  }

  // relaciones (antes que las cajas)
  const marcas = [], etiquetas = [];
  g.edges.forEach((e, i) => {
    const r = modelo.relaciones[i], t = TIPOS[r.flecha];
    const o = abs.get(e.container || "raiz") || { x: 0, y: 0 };
    const sec = e.sections[0];
    const pts = [sec.startPoint, ...(sec.bendPoints || []), sec.endPoint].map((q) => ({ x: q.x + o.x, y: q.y + o.y }));
    const col = FAMILIA[t.familia].color;
    const guion = t.trazo ? ` stroke-dasharray="${t.trazo}"` : "";
    const sujeto = r.flecha.startsWith("<|") ? r.b : r.a;
    const attrs = `data-a="${r.a}" data-b="${r.b}" data-sujeto="${sujeto}" data-frase="${esc(frase(r))}"`;
    out.push(`<g class="rel" ${attrs}><path d="${trazo(pts)}" fill="none" stroke="${col}" stroke-width="${t.familia === "uso" ? 1.1 : 1.4}"${guion}/></g>`);
    marcas.push(`<g class="rel" data-a="${r.a}" data-b="${r.b}">` +
      (t.inicio ? marca(t.inicio, pts[0], pts[1], col) : "") +
      (t.fin ? marca(t.fin, pts[pts.length - 1], pts[pts.length - 2], col) : "") + `</g>`);
    etiquetas.push(`<g class="rel" data-a="${r.a}" data-b="${r.b}">`);
    for (const l of e.labels || []) {
      const x = l.x + o.x, y = l.y + o.y;
      const esMult = l.text !== r.verbo;
      etiquetas.push(
        `<rect x="${x}" y="${y}" width="${l.width}" height="${l.height}" rx="2" fill="${C.paper}" fill-opacity="0.92"/>` +
        `<text x="${x + l.width / 2}" y="${y + l.height - 3.5}" text-anchor="middle" font-family="${MONO}" font-size="${ETIQUETA.size}" fill="${esMult ? C.ink : t.familia === "uso" ? C.soft : col}"${esMult ? ` font-weight="600"` : ` font-style="italic"`}>${esc(l.text)}</text>`,
      );
    }
    etiquetas.push(`</g>`);
  });

  for (const [id, c] of modelo.clases) {
    const a = abs.get(id);
    out.push(caja(c, medidas.get(id), a.x, a.y));
  }
  out.push(...marcas, ...etiquetas);
  out.push(`</g>`);
  out.push(`<style>${ESTILO}</style>`);
  out.push(`<script><![CDATA[${INTERACCION}]]></script>`);
  out.push(`</svg>`);
  return out.join("\n");
}

// "Credito [1] se divide en [1..*] Cuota"; implementa y extiende se leen de la hija al padre
function frase(r) {
  const m = (x) => (x ? ` [${x}]` : "");
  if (r.flecha.startsWith("<|")) return `${r.b} ${r.verbo} ${r.a}`;
  return `${r.a}${m(r.multA)} ${r.verbo}${m(r.multB)} ${r.b}`;
}

const ESTILO = `
svg[data-dominio] .clase { cursor: pointer; }
svg[data-dominio] .clase, svg[data-dominio] .rel { transition: opacity .15s; }
svg[data-dominio].foco .clase:not(.activa), svg[data-dominio].foco .rel:not(.activa) { opacity: .1; }
svg[data-dominio].foco .rel.activa path { stroke-width: 2.4; }
svg[data-dominio] .clase.elegida > rect:first-child { stroke: #bf4520; stroke-width: 2; }
svg[data-dominio] .btn-patron { cursor: pointer; }
svg[data-dominio] .btn-patron.elegido rect { stroke: ${C.ink}; stroke-width: 2.5; }
svg[data-dominio].patron .rel.activa path { stroke: var(--patron); stroke-width: 2.6; }
svg[data-dominio].patron .clase.activa > rect:first-child { stroke: var(--patron); stroke-width: 2.4; }
`;

// Hover: deja a la vista la clase, sus vecinas y sus relaciones. Clic fija, Esc o clic afuera limpia.
// Listeners delegados en document: sirven tambien para la copia que abre el zoom del reporte.
const INTERACCION = `
(function () {
  if (window.__dominio) return;
  window.__dominio = true;
  var fijada = null, panel = null;
  function svgDe(el) { return el && el.closest ? el.closest("svg[data-dominio]") : null; }
  function mostrarPanel(id, rels) {
    if (!document.body) return;
    if (!panel) {
      panel = document.createElement("div");
      panel.style.cssText = "position:fixed;right:16px;bottom:16px;max-width:420px;max-height:60vh;overflow:auto;z-index:10000;" +
        "background:#fff;border:1px solid #bfc0c0;border-radius:6px;padding:10px 14px;font:12px/1.5 'Geist Mono',ui-monospace,Menlo,monospace;color:#2d3142";
      document.body.appendChild(panel);
    }
    var sal = [], ent = [];
    rels.forEach(function (r) { (r.getAttribute("data-sujeto") === id ? sal : ent).push(r.getAttribute("data-frase")); });
    function lista(t, xs) { return xs.length ? "<div style='margin-top:6px;color:#5a6580'>" + t + "</div>" + xs.map(function (x) { return "<div>" + x.replace(/</g, "&lt;") + "</div>"; }).join("") : ""; }
    panel.innerHTML = "<div style='font:600 14px Geist,sans-serif'>" + id + (fijada ? " <span style='font-weight:400;color:#5a6580'>(fijada, Esc para soltar)</span>" : "") + "</div>" +
      lista("Qué hace " + id, sal) + lista("Quién usa a " + id, ent);
    panel.style.display = "block";
  }
  function limpiar(svg) {
    svg.classList.remove("foco", "patron");
    svg.querySelectorAll(".btn-patron.elegido").forEach(function (b) { b.classList.remove("elegido"); });
    svg.querySelectorAll(".activa, .elegida").forEach(function (e) { e.classList.remove("activa", "elegida"); });
    if (panel) panel.style.display = "none";
  }
  function enfocar(svg, id) {
    limpiar(svg);
    svg.classList.add("foco");
    var vecinas = {}; vecinas[id] = true;
    var trazos = [];
    svg.querySelectorAll(".rel").forEach(function (r) {
      var a = r.getAttribute("data-a"), b = r.getAttribute("data-b");
      if (a === id || b === id) {
        r.classList.add("activa"); vecinas[a] = vecinas[b] = true;
        if (r.hasAttribute("data-frase")) trazos.push(r);
      }
    });
    svg.querySelectorAll(".clase").forEach(function (c) {
      if (vecinas[c.getAttribute("data-id")]) c.classList.add("activa");
      if (c.getAttribute("data-id") === id) c.classList.add("elegida");
    });
    mostrarPanel(id, trazos);
  }
  document.addEventListener("mouseover", function (e) {
    var c = e.target.closest && e.target.closest(".clase"), svg = svgDe(c);
    if (svg && !fijada) enfocar(svg, c.getAttribute("data-id"));
  });
  document.addEventListener("mouseout", function (e) {
    var c = e.target.closest && e.target.closest(".clase"), svg = svgDe(c);
    if (svg && !fijada && !(e.relatedTarget && c.contains(e.relatedTarget))) limpiar(svg);
  });
  function enfocarPatron(svg, btn) {
    limpiar(svg);
    svg.classList.add("foco", "patron");
    svg.style.setProperty("--patron", btn.getAttribute("data-color"));
    btn.classList.add("elegido");
    var en = {};
    btn.getAttribute("data-clases").split(",").forEach(function (id) { en[id] = true; });
    svg.querySelectorAll(".clase").forEach(function (c) { if (en[c.getAttribute("data-id")]) c.classList.add("activa"); });
    svg.querySelectorAll(".rel").forEach(function (r) { if (en[r.getAttribute("data-a")] && en[r.getAttribute("data-b")]) r.classList.add("activa"); });
    if (!document.body) return;
    mostrarPanel("", []);
    panel.innerHTML = btn.getAttribute("data-ficha") + "<div style='margin-top:6px;color:#5a6580'>Esc o clic afuera para salir</div>";
  }
  document.addEventListener("click", function (e) {
    var b = e.target.closest && e.target.closest(".btn-patron"), sb = svgDe(b);
    if (sb) {
      e.stopPropagation();
      var nombre = "patron:" + b.getAttribute("data-patron");
      fijada = fijada === nombre ? null : nombre;
      if (fijada) enfocarPatron(sb, b); else limpiar(sb);
      return;
    }
    var c = e.target.closest && e.target.closest(".clase"), svg = svgDe(c);
    if (svg) {
      e.stopPropagation();
      var id = c.getAttribute("data-id");
      fijada = fijada === id ? null : id;
      if (fijada) enfocar(svg, id); else limpiar(svg);
    } else if (fijada) {
      fijada = null;
      document.querySelectorAll("svg[data-dominio]").forEach(limpiar);
    }
  }, true);
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && fijada) { fijada = null; document.querySelectorAll("svg[data-dominio]").forEach(limpiar); }
  });
})();
`;

const modelo = leerModelo();
const medidas = new Map([...modelo.clases.values()].map((c) => [c.id, medir(c)]));
const g = await disponer(modelo, medidas);
writeFileSync(SALIDA, dibujar(modelo, medidas, g));
console.log(`${modelo.clases.size} clases, ${modelo.relaciones.length} relaciones -> ${SALIDA}`);
