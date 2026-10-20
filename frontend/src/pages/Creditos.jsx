import { formatMoneda, formatFecha } from '../utils/formato';
import { CARGOS } from '../utils/cargos';
import Aviso from '../components/Aviso';
import { getCredito } from '../api/creditos';
import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCreditosPorCliente, addCredito, clearCreditos, anularCreditoThunk } from '../store/slices/creditosSlice';

const ESTADO_CUOTA = {
  pagada:    { color: 'var(--color-success)', texto: '✔ Pagada' },
  vencida:   { color: 'var(--color-danger)',  texto: '✘ Vencida' },
  pendiente: { color: 'var(--color-warning)', texto: '… Pendiente' },
};
const estadoCuota = (c) => {
  if (c.pagada) return ESTADO_CUOTA.pagada;
  if (c.vencida) return ESTADO_CUOTA.vencida;
  return ESTADO_CUOTA.pendiente;
};

const FORM_VACIO = { dniCliente:'', deudaOriginal:'', fecha:'', tasaInteres:'', cantidadCuotas:'', tipoPlan:'INTERES_SIMPLE', cargos:[] };

const descripcionPlan = (cr) => `${cr.tipoPlan === 'SISTEMA_FRANCES' ? 'Sistema francés' : 'Interés simple'} · ${cr.tasaInteres}% mensual`;

const descripcionCargos = (cr) => {
  const etiquetas = Object.keys(CARGOS).filter(c => cr.cargos?.includes(c)).map(c => CARGOS[c].etiqueta);
  return etiquetas.length ? etiquetas.join(', ') : 'Sin cargos';
};

// Creditos de un cliente y consulta de uno por numero: otorgamiento, plan de
// cuotas con su estado y anulacion.
export default function Creditos() {
  const dispatch = useDispatch();
  
  const { user } = useSelector((state) => state.auth);
  const { lista: creditos, loading, error } = useSelector((state) => state.creditos);
  
  const [dni, setDni] = useState('');
  const [dniBuscado, setDniBuscado] = useState('');
  const [buscado, setBuscado] = useState(false);
  const [errorBusqueda, setErrorBusqueda] = useState(null);
  const [exito, setExito] = useState(null);
  const [idConsulta, setIdConsulta] = useState('');
  const [detalle, setDetalle] = useState(null);
  const [errorDetalle, setErrorDetalle] = useState(null);
  const [form, setForm] = useState(FORM_VACIO);

  const buscar = async (e) => {
    e.preventDefault();
    dispatch(clearCreditos());
    setErrorBusqueda(null);
    try {
      await dispatch(fetchCreditosPorCliente(dni)).unwrap();
      setDniBuscado(dni);
      setBuscado(true);
    } catch (err) {
      setBuscado(false);
      setErrorBusqueda(err);
    }
  };

  const alternarCargo = (cargo) => setForm({
    ...form,
    cargos: form.cargos.includes(cargo) ? form.cargos.filter(c => c !== cargo) : [...form.cargos, cargo],
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      deudaOriginal: Number(form.deudaOriginal),
      tasaInteres: Number(form.tasaInteres),
      cantidadCuotas: Number(form.cantidadCuotas),
    };
    setExito(null);
    const result = await dispatch(addCredito(payload));
    if (result.meta.requestStatus === 'fulfilled') {
      setExito(`Crédito #${result.payload.id} creado para el DNI ${form.dniCliente}.`);
      setForm(FORM_VACIO);
      if (buscado && form.dniCliente === dniBuscado) dispatch(fetchCreditosPorCliente(dniBuscado));
    }
  };

  const handleAnular = async (id) => {
    if (window.confirm("¿Estás seguro de anular este crédito?")) {
      try {
        await dispatch(anularCreditoThunk(id)).unwrap();
        if (buscado) dispatch(fetchCreditosPorCliente(dniBuscado));
        if (detalle?.id === id) setDetalle(await getCredito(id));
      } catch (err) {
        alert("Error: " + err); 
      }
    }
  };

  const consultar = async (e) => {
    e.preventDefault();
    setDetalle(null);
    setErrorDetalle(null);
    try {
      setDetalle(await getCredito(idConsulta));
    } catch (err) {
      setErrorDetalle(err.message);
    }
  };

  const fichaCredito = (cr) => (
    <div key={cr.id} style={{ ...styles.creditoBox, opacity: cr.anulado ? 0.6 : 1 }}>
      <div style={styles.creditoHeader}>
        <strong>Crédito #{cr.id}</strong>
        <span style={{ ...styles.badge, ...(estadoColores[cr.estado] || {}) }}>{cr.estado}</span>
        <span style={styles.progreso}>
          {cr.cuotas.filter(c => c.pagada).length} de {cr.cantidadCuotas} cuotas pagadas
        </span>
      </div>
      <dl style={styles.datos}>
        <div><dt style={styles.dt}>Capital prestado</dt><dd style={styles.dd}>{formatMoneda(cr.deudaOriginal)}</dd></div>
        <div><dt style={styles.dt}>Plan</dt><dd style={styles.dd}>{descripcionPlan(cr)}</dd></div>
        <div><dt style={styles.dt}>Otorgado</dt><dd style={styles.dd}>{formatFecha(cr.fecha)}</dd></div>
        <div><dt style={styles.dt}>Total a devolver</dt><dd style={styles.dd}>{formatMoneda(cr.totalADevolver)}</dd></div>
        <div>
          <dt style={styles.dt}>Cuota</dt>
          <dd style={styles.dd}>{cr.cantidadCuotas} × {formatMoneda(cr.importeCuota)}</dd>
          <table style={styles.desglose}>
            <tbody>
              {cr.desgloseCuota.map((r, i) => (
                <tr key={r.concepto}>
                  <td style={styles.renglon}>{i > 0 && '+'}</td>
                  <td style={{ ...styles.renglon, textAlign:'right' }}>{formatMoneda(r.importe)}</td>
                  <td style={styles.renglon}>{r.concepto}</td>
                </tr>
              ))}
              <tr>
                <td style={styles.totalDesglose}>=</td>
                <td style={{ ...styles.totalDesglose, textAlign:'right' }}>{formatMoneda(cr.importeCuota)}</td>
                <td style={styles.totalDesglose}></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div><dt style={styles.dt}>Cargos</dt><dd style={styles.dd}>{descripcionCargos(cr)}</dd></div>
        <div><dt style={styles.dt}>Saldo</dt><dd style={{ ...styles.dd, fontWeight: 'bold' }}>{formatMoneda(cr.saldo)}</dd></div>
      </dl>

      {cr.puedeAnularse && user?.puedeAnularCredito && (
        <button onClick={() => handleAnular(cr.id)} style={styles.btnAnular}>
          Anular
        </button>
      )}

      <table style={styles.table}>
        <thead>
          <tr>
            <th style={{textAlign: 'left'}}>#</th>
            <th style={{textAlign: 'left'}}>Vencimiento</th>
            <th style={{textAlign: 'right', paddingRight: '24px'}}>Importe</th>
            <th style={{textAlign: 'left'}}>Estado</th>
          </tr>
        </thead>
        <tbody>
          {cr.cuotas.map(c => (
            <tr key={c.numeroCuota}>
              <td style={{padding: '5px 0'}}>{c.numeroCuota}</td>
              <td>{formatFecha(c.fechaVencimiento)}</td>
              <td style={{textAlign: 'right', paddingRight: '24px'}}>{formatMoneda(c.importe)}</td>
              <td style={{ color: estadoCuota(c).color, fontWeight: 'bold' }}>
                {estadoCuota(c).texto}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  return (
    <div style={styles.page}>
      <h2 style={styles.title}>Créditos</h2>

      <div style={styles.card}>
        <h3>Buscar créditos por cliente</h3>
        <form onSubmit={buscar} style={styles.row}>
          <input 
            style={styles.input} 
            placeholder="DNI del cliente" 
            value={dni} 
            onChange={e => setDni(e.target.value)} 
            required 
          />
          <button style={styles.btn}>Buscar</button>
        </form>
        {errorBusqueda && <div style={{ marginTop: '12px' }}><Aviso>{errorBusqueda}</Aviso></div>}
      </div>

      <div style={styles.card}>
        <h3>Consultar crédito por número</h3>
        <form onSubmit={consultar} style={styles.row}>
          <input style={styles.input} placeholder="Nro. de crédito" type="number" min="1" value={idConsulta} onChange={e => setIdConsulta(e.target.value)} required />
          <button style={styles.btn}>Consultar</button>
        </form>
        {errorDetalle && <div style={{ marginTop: '12px' }}><Aviso>{errorDetalle}</Aviso></div>}
        {detalle && <div style={{ marginTop: '16px' }}>{fichaCredito(detalle)}</div>}
      </div>

      <div style={styles.card}>
        <h3>Nuevo crédito</h3>
        <Aviso>{error}</Aviso>
        <Aviso tipo="exito">{exito}</Aviso>
        <form onSubmit={handleSubmit} style={styles.grid}>
          <input style={styles.input} placeholder="DNI cliente" value={form.dniCliente} onChange={e => setForm({...form, dniCliente: e.target.value})} required />
          <input style={styles.input} placeholder="Capital prestado" value={form.deudaOriginal} onChange={e => setForm({...form, deudaOriginal: e.target.value})} type="number" required />
          <input style={styles.input} placeholder="Fecha" value={form.fecha} onChange={e => setForm({...form, fecha: e.target.value})} type="date" required />
          <input style={styles.input} placeholder="Interés % mensual (ej: 3)" value={form.tasaInteres} onChange={e => setForm({...form, tasaInteres: e.target.value})} type="number" min="0" step="0.01" required />
          <input style={styles.input} placeholder="Cant. cuotas" value={form.cantidadCuotas} onChange={e => setForm({...form, cantidadCuotas: e.target.value})} type="number" min="1" required />
          <select style={styles.input} value={form.tipoPlan} onChange={e => setForm({...form, tipoPlan: e.target.value})}>
            <option value="INTERES_SIMPLE">Interés simple</option>
            <option value="SISTEMA_FRANCES">Sistema francés</option>
          </select>
          {Object.entries(CARGOS).map(([cargo, { etiqueta, detalle }]) => (
            <label key={cargo} style={styles.casilla}>
              <input type="checkbox" checked={form.cargos.includes(cargo)} onChange={() => alternarCargo(cargo)} />
              {etiqueta} ({detalle})
            </label>
          ))}
          <button style={{...styles.btn, gridColumn:'span 2'}} disabled={loading}>
            {loading ? 'Guardando...' : 'Crear crédito'}
          </button>
        </form>
      </div>

      {buscado && (
        <div style={styles.card}>
          <h3>Créditos del cliente ({creditos.length})</h3>
          
          {loading && <p style={styles.empty}>Cargando...</p>}
          {!loading && creditos.length === 0 && <p style={styles.empty}>Sin créditos.</p>}
          
          {creditos.map(fichaCredito)}
        </div>
      )}
    </div>
  );
}

const styles = {
  page:         { padding:'32px', maxWidth:'900px', margin:'0 auto', fontFamily: 'Arial, sans-serif' },
  title:        { color:'var(--color-heading)', marginBottom:'24px', borderBottom: '2px solid var(--color-border)', paddingBottom: '10px' },
  card:         { background:'var(--color-surface)', padding:'24px', borderRadius:'12px', boxShadow:'0 2px 10px var(--color-shadow)', marginBottom:'24px' },
  row:          { display:'flex', gap:'12px' },
  grid:         { display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px' },
  casilla:      { display:'flex', alignItems:'center', gap:'8px' },
  input:        { padding:'10px', border:'1px solid var(--color-border-strong)', borderRadius:'6px', width:'100%', boxSizing:'border-box' },
  btn:          { padding:'10px 20px', backgroundColor:'var(--color-primary)', color:'var(--color-on-primary)', border:'none', borderRadius:'6px', cursor:'pointer', fontWeight:'bold' },
  btnAnular:    { background: 'var(--color-danger-solid)', color: 'var(--color-on-primary)', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', marginBottom: '10px', fontSize: '0.85em' },
  empty:        { color:'var(--color-text-muted)', fontStyle: 'italic' },
  creditoBox:   { borderLeft:'4px solid var(--color-primary)', paddingLeft:'16px', marginBottom:'20px', paddingBottom: '15px', borderBottom: '1px solid var(--color-border)' },
  table:        { width:'100%', borderCollapse:'collapse', marginTop:'8px', fontSize: '0.9em' },
  creditoHeader: { display:'flex', alignItems:'center', gap:'12px', marginBottom:'12px' },
  badge:        { padding:'3px 10px', borderRadius:'12px', fontSize:'0.75em', fontWeight:'bold', letterSpacing:'0.5px' },
  progreso:     { marginLeft:'auto', color:'var(--color-text-muted)', fontSize:'0.9em' },
  datos:        { display:'grid', gridTemplateColumns:'repeat(3, 1fr)', gap:'12px 24px', margin:'0 0 16px 0' },
  dt:           { color:'var(--color-text-muted)', fontSize:'0.8em', marginBottom:'2px' },
  dd:           { margin:0 },
  desglose:     { borderCollapse:'collapse', marginTop:'4px', fontSize:'0.8em', color:'var(--color-text-muted)' },
  renglon:      { padding:'1px 6px 1px 0', border:'none', whiteSpace:'nowrap' },
  totalDesglose: { padding:'2px 6px 1px 0', border:'none', borderTop:'1px solid var(--color-border)', fontWeight:'bold', whiteSpace:'nowrap' },
};

const estadoColores = {
  VIGENTE:   { background:'var(--color-info-bg)', color:'var(--color-info)' },
  CANCELADO: { background:'var(--color-success-bg)', color:'var(--color-success)' },
  ANULADO:   { background:'var(--color-danger-bg)', color:'var(--color-danger)' },
};