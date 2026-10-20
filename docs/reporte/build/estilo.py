CSS = '''
:root{--navy:#1D2B4F;--azul:#2E75B6;--azul-claro:#EEF4FB;--texto:#222;--gris:#666;--linea:#D9E1EC;--fondo:#fff;--rojo:#B3261E;--ambar:#B38600;--verde:#2E7D32}
*{box-sizing:border-box}
body{margin:0;background:var(--fondo);color:var(--texto);font-family:"Calibri Light",Calibri,Carlito,"Segoe UI",system-ui,sans-serif;font-size:17px;line-height:1.5}
#barra{position:sticky;top:0;z-index:5;background:var(--fondo);border-bottom:1px solid var(--linea);display:flex;gap:12px;align-items:center;padding:8px 24px;flex-wrap:wrap}
#barra nav{flex:1;display:flex;gap:14px;flex-wrap:wrap;font-size:14px}
#barra a{color:var(--azul);text-decoration:none}
button{font:inherit;font-size:14px;background:var(--azul);color:#fff;border:0;border-radius:4px;padding:6px 14px;cursor:pointer}
button.sec{background:transparent;color:var(--azul);border:1px solid var(--azul)}
main{max-width:1180px;margin:0 auto;padding:0 24px 80px}
section{padding-top:48px}
h1,h2,h3,h4{color:var(--navy);font-weight:300;margin:0}
h2{font-size:34px;padding-bottom:10px;margin-bottom:24px;position:relative}
h2::after{content:"";position:absolute;left:0;bottom:0;width:48px;height:3px;background:var(--azul)}
h3{font-size:24px;font-weight:400;margin:0 0 10px}
.portada{min-height:520px;display:flex;flex-direction:column;justify-content:center;border-bottom:1px solid var(--linea)}
.portada .marca{font-family:Georgia,"Times New Roman",serif;font-weight:700;letter-spacing:.04em;color:var(--navy);font-size:32px;align-self:flex-end}
.portada h1{font-size:54px;line-height:1.1;margin:24px 0 12px}
.portada .sub{font-size:24px;color:var(--gris)}
.portada .rule{width:64px;height:4px;background:var(--azul);margin:20px 0}
.portada ul{list-style:none;padding:0;margin:20px 0 0}
.portada li{padding:2px 0}
.timeline{display:grid;grid-template-columns:repeat(5,1fr);gap:0;position:relative;margin:24px 0 16px}
.timeline::before{content:"";position:absolute;left:0;right:0;top:11px;height:3px;background:var(--azul)}
.hito{position:relative;padding:32px 14px 0 0}
.hito .punto{position:absolute;top:2px;left:0;width:20px;height:20px;border-radius:50%;background:var(--fondo);border:3px solid var(--azul)}
.hito.actual .punto{background:var(--azul)}
.hito h4{font-size:30px}
.hito .tema{margin:0;font-weight:600;color:var(--navy)}
.hito .meta{margin:2px 0 8px;font-size:14px;color:var(--gris)}
.hito ul{margin:0;padding-left:18px;font-size:15px}
code{font-family:"SF Mono",Menlo,Consolas,monospace;font-size:.86em;background:var(--azul-claro);padding:1px 5px;border-radius:3px}
pre{background:var(--azul-claro);border:1px solid var(--linea);border-left:3px solid var(--azul);border-radius:4px;padding:10px 14px;overflow-x:auto;margin:8px 0;font-size:13px;line-height:1.45}
pre code{background:none;padding:0;font-size:inherit}
pre.vacio{border-left-color:var(--ambar);color:var(--gris)}
table{border-collapse:collapse;width:100%;font-size:15px;margin:12px 0}
th{background:var(--navy);color:var(--fondo);font-weight:400;text-align:left}
th,td{padding:7px 10px;border-bottom:1px solid var(--linea);vertical-align:top}
.mejora{border:1px solid var(--linea);border-radius:6px;padding:20px 24px;margin:0 0 28px}
.badge{font-size:12px;font-weight:600;border-radius:10px;padding:2px 10px;vertical-align:middle;margin-left:8px;border:1px solid}
.badge.prop{color:var(--azul)}.badge.pend{color:var(--ambar)}.badge.impl{color:var(--verde)}.badge.plan{color:var(--gris)}
.ref{color:var(--gris);font-size:14px;margin:0 0 10px}
dl.estructura{margin:0;display:grid;grid-template-columns:200px 1fr;gap:12px 20px}
dl.estructura dt{color:var(--azul);font-weight:600}
dl.estructura dd{margin:0;min-width:0}
dd p{margin:0 0 6px}
.par{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-top:18px}
figure{margin:0;min-width:0}
figcaption{font-weight:600;color:var(--navy);margin-bottom:6px}
.svgbox{background:#fff;border:1px solid var(--linea);border-radius:4px;padding:8px;overflow-x:auto}
.svgbox svg{max-width:100%;height:auto;display:block;margin:0 auto}
details summary{cursor:pointer;color:var(--azul);font-size:14px;margin-top:6px}
.mvcsvg{background:#fff;border:1px solid var(--linea);border-radius:6px;padding:10px;margin:16px 0}
.mvcsvg svg{width:100%;height:auto}
.nota{border-left:3px solid var(--azul);background:var(--azul-claro);padding:10px 16px;margin:14px 0}
[contenteditable]:focus{outline:2px dashed var(--azul);outline-offset:2px}
@media (max-width:820px){.timeline{grid-template-columns:1fr}.timeline::before{display:none}.par{grid-template-columns:1fr}dl.estructura{grid-template-columns:1fr}.portada h1{font-size:38px}}
@media print{
 @page{size:A4;margin:14mm}
 #barra{display:none}
 :root{--fondo:#fff;--texto:#222;--navy:#1D2B4F;--azul:#2E75B6;--azul-claro:#EEF4FB;--linea:#D9E1EC;--gris:#666}
 body{font-size:11pt}
 main{max-width:none;padding:0}
 section{break-before:page;padding-top:0}
 section.portada{break-before:auto;min-height:600px}
 .mejora,figure,pre,table,.hito{break-inside:avoid}
 .par{grid-template-columns:1fr 1fr;gap:10px}
 details:not([open])>pre{display:none}
 details summary{display:none}
}
'''
CSS += '''
.ruta{font-family:"SF Mono",Menlo,Consolas,monospace;font-size:12px;color:var(--navy);background:var(--linea);border-radius:4px 4px 0 0;padding:4px 10px;margin:10px 0 0;word-break:break-all}
.ruta + pre{margin-top:0;border-top-left-radius:0}
.ruta .ver{color:var(--gris)}
.cuadro{display:grid;grid-template-columns:1fr 1fr;gap:16px;margin:14px 0}
.cuadro>div{border:1px solid var(--linea);border-radius:6px;padding:12px 16px}
.cuadro h4{font-size:18px;font-weight:600;margin-bottom:6px}
.qa dt{font-weight:600;color:var(--navy);margin-top:10px}.qa dd{margin:2px 0 0 0}
.rev{border-left:3px solid var(--linea);padding:4px 0 4px 16px;margin:0 0 16px}
.rev h4{font-size:18px;font-weight:600}
.rev p{margin:2px 0;font-size:15.5px}
.clases{border:1px solid var(--linea);border-radius:6px;background:#fff;padding:8px;margin:8px 0 18px}
.clases svg{display:block;width:100%!important;height:auto!important;max-width:100%}
.tec dt{font-weight:600;color:var(--navy)}.tec dd{margin:0 0 8px 0}
@media (max-width:820px){.cuadro{grid-template-columns:1fr}}
/* Diagramas clickeables: click abre el visor con zoom (rueda, +/-, 0) y arrastre; Esc cierra. */
.zoomable{cursor:zoom-in;position:relative}
.zoomable::after{content:"click para ampliar";position:absolute;top:6px;right:8px;font-size:11px;letter-spacing:.04em;text-transform:uppercase;color:var(--gris);background:#fff;border:1px solid var(--linea);border-radius:3px;padding:1px 6px;opacity:.6;pointer-events:none}
.zoomable:hover::after{opacity:1;color:var(--azul);border-color:var(--azul)}
body.lb-locked{overflow:hidden}
.lightbox{display:none;position:fixed;inset:0;z-index:50;flex-direction:column;background:rgba(15,20,35,.88)}
.lightbox.open{display:flex}
.lb-bar{display:flex;align-items:center;gap:8px;padding:8px 16px;background:var(--fondo);border-bottom:1px solid var(--linea)}
.lb-title{margin-right:auto;font-weight:600;color:var(--navy);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.lb-zoom{font-size:13px;color:var(--gris);min-width:3.6em;text-align:center}
.lb-scroll{flex:1;overflow:auto;padding:28px;cursor:grab}
.lb-scroll.grabbing{cursor:grabbing}
.lb-canvas{background:#fff;border-radius:4px;margin:0 auto}
.lb-canvas svg{display:block;width:100%!important;height:100%!important;max-width:none}
@media print{.zoomable::after{display:none}.lightbox{display:none!important}}
'''
