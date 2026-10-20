# Exploración de patrones de la iteración 4: exploracion.md -> index.html, con cada bloque PlantUML renderizado a SVG.
# Uso: python3 build.py
import os, re, subprocess

AQUI = os.path.dirname(os.path.abspath(__file__))
PUML = os.path.join(AQUI, "puml")
BLOQUE = re.compile(r"```plantuml\n(.*?)```", re.S)


def main():
    md = open(os.path.join(AQUI, "exploracion.md"), encoding="utf-8").read()
    svgs = []
    def reemplazar(m):
        n = len(svgs) + 1
        fuente = m.group(1).replace("@startuml\n", "@startuml\n!include _estilo.iuml\n", 1)
        open(os.path.join(PUML, f"d{n:02d}.puml"), "w", encoding="utf-8").write(fuente)
        svgs.append(f"d{n:02d}")
        return f"\n\nDIAGRAMA{n:02d}\n\n"
    md = BLOQUE.sub(reemplazar, md)
    subprocess.run(["plantuml", "-nometadata", "-tsvg", *[s + ".puml" for s in svgs]], cwd=PUML, check=True)
    cuerpo = subprocess.run(["pandoc", "-f", "gfm", "-t", "html"], input=md, capture_output=True, text=True, check=True).stdout
    for s in svgs:
        svg = open(os.path.join(PUML, s + ".svg"), encoding="utf-8").read()
        svg = re.sub(r"<\?(xml|plantuml).*?\?>", "", svg, flags=re.S)
        cuerpo = cuerpo.replace(f"<p>DIAGRAMA{s[1:]}</p>", f'<div class="diag zoomable" title="Click para ampliar">{svg}</div>')
    html = PLANTILLA.replace("{{CUERPO}}", cuerpo)
    open(os.path.join(AQUI, "index.html"), "w", encoding="utf-8").write(html)
    print("index.html generado,", len(svgs), "diagramas")


PLANTILLA = """<!doctype html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Exploración iteración 4</title>
<style>
:root { --ink:#1D2B4F; --muted:#555f73; --rule:#D9E1EC; --accent:#2E75B6; --paper:#ffffff; --soft:#F4F7FB; }
@media (prefers-color-scheme: dark) { :root { --ink:#E6ECF5; --muted:#A8B3C7; --rule:#2B3550; --accent:#6FA8DC; --paper:#121826; --soft:#1A2236; } }
* { box-sizing:border-box; }
body { margin:0; background:var(--paper); color:var(--ink); font:16px/1.55 Calibri, Carlito, "Segoe UI", system-ui, sans-serif; }
main { max-width:1040px; margin:0 auto; padding:24px 24px 64px; }
a { color:var(--accent); }
h1 { font-size:30px; } h2 { font-size:26px; margin-top:48px; border-bottom:3px solid var(--accent); padding-bottom:6px; }
h3 { font-size:21px; margin-top:36px; border-bottom:1px solid var(--rule); padding-bottom:4px; } h4 { font-size:18px; margin-top:28px; color:var(--accent); }
code { background:var(--soft); padding:1px 4px; border-radius:3px; font-size:14px; }
pre { background:var(--soft); padding:12px; border-radius:6px; overflow:auto; } pre code { padding:0; }
blockquote { margin:12px 0; padding:8px 14px; border-left:3px solid var(--accent); background:var(--soft); }
table { width:100%; border-collapse:collapse; margin:14px 0; font-size:15px; }
th, td { text-align:left; vertical-align:top; padding:6px 9px; border-bottom:1px solid var(--rule); }
th { color:var(--muted); }
.diag { background:#fff; border:1px solid var(--rule); border-radius:6px; padding:12px; text-align:center; cursor:zoom-in; overflow:auto; margin:12px 0; }
.diag svg { max-width:100%; height:auto; }
#lb { display:none; position:fixed; inset:0; background:rgba(0,0,0,.75); z-index:10; align-items:center; justify-content:center; }
#lb.on { display:flex; }
#lb div { background:#fff; padding:16px; border-radius:6px; max-width:96vw; max-height:94vh; overflow:auto; }
#lb svg { width:auto; height:auto; min-width:min(1100px,90vw); }
</style></head><body><main>
{{CUERPO}}
</main>
<div id="lb"><div></div></div>
<script>
const lb=document.getElementById('lb');
document.querySelectorAll('.zoomable').forEach(d=>d.addEventListener('click',()=>{lb.firstElementChild.innerHTML=d.innerHTML;lb.classList.add('on');}));
lb.addEventListener('click',()=>lb.classList.remove('on'));
document.addEventListener('keydown',ev=>{if(ev.key==='Escape')lb.classList.remove('on');});
</script>
</body></html>
"""

if __name__ == "__main__":
    main()
