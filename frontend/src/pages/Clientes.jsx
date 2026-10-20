import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchClientes, addCliente } from '../store/slices/clientesSlice';
import { getCliente, getDeudaCliente } from '../api/clientes';
import { formatMoneda } from '../utils/formato';
import Aviso from '../components/Aviso';

const FORM_VACIO = { dni: '', nombre: '' };

// Alta, busqueda por DNI y listado de clientes.
export default function Clientes() {
  const dispatch = useDispatch();
  const { lista, loading, error } = useSelector((state) => state.clientes);
  const [form, setForm] = useState(FORM_VACIO);
  const [dniBuscado, setDniBuscado] = useState('');
  const [encontrado, setEncontrado] = useState(null);
  const [deuda, setDeuda] = useState(null);
  const [errorBusqueda, setErrorBusqueda] = useState(null);

  const buscar = async (e) => {
    e.preventDefault();
    setEncontrado(null);
    setDeuda(null);
    setErrorBusqueda(null);
    try {
      setEncontrado(await getCliente(dniBuscado));
      setDeuda(await getDeudaCliente(dniBuscado));
    } catch (err) {
      setErrorBusqueda(err.message);
    }
  };

  useEffect(() => { dispatch(fetchClientes()); }, [dispatch]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(addCliente(form));
    if (result.meta.requestStatus === 'fulfilled') setForm(FORM_VACIO);
  };

  return (
    <div style={styles.page}>
      <h2 style={styles.title}>Clientes</h2>

      <div style={styles.card}>
        <h3>Buscar cliente por DNI</h3>
        <form onSubmit={buscar} style={styles.form}>
          <input style={styles.input} placeholder="DNI" value={dniBuscado} onChange={e => setDniBuscado(e.target.value)} required />
          <button style={styles.btn}>Buscar</button>
        </form>
        {errorBusqueda && <div style={{ marginTop: '12px' }}><Aviso>{errorBusqueda}</Aviso></div>}
        {encontrado && <p style={{ marginTop: '12px' }}><strong>{encontrado.nombre}</strong> · DNI {encontrado.dni}</p>}
        {deuda && (
          <>
            <p>Saldo pendiente: <strong>{formatMoneda(deuda.saldo)}</strong></p>
            {Number(deuda.montoVencido) > 0
              ? <Aviso>Vencido: <strong>{formatMoneda(deuda.montoVencido)}</strong></Aviso>
              : <p>Vencido: {formatMoneda(deuda.montoVencido)}</p>}
          </>
        )}
      </div>

      <div style={styles.card}>
        <h3>Nuevo cliente</h3>
        <Aviso>{error}</Aviso>
        <form onSubmit={handleSubmit} style={styles.form}>
          <input style={styles.input} placeholder="DNI" value={form.dni} onChange={e => setForm({...form, dni: e.target.value})} required />
          <input style={styles.input} placeholder="Nombre completo" value={form.nombre} onChange={e => setForm({...form, nombre: e.target.value})} required />
          <button style={styles.btn} disabled={loading}>{loading ? 'Guardando...' : 'Agregar'}</button>
        </form>
      </div>

      <div style={styles.card}>
        <h3>Lista de clientes ({lista.length})</h3>
        {loading && <p style={styles.empty}>Cargando...</p>}
        {!loading && lista.length === 0 && <p style={styles.empty}>No hay clientes registrados.</p>}
        {lista.length > 0 && (
          <table style={styles.table}>
            <thead><tr style={styles.trHead}><th style={styles.th}>DNI</th><th style={styles.th}>Nombre</th></tr></thead>
            <tbody>
              {lista.map(c => (
                <tr key={c.dni} style={styles.tr}><td style={styles.td}>{c.dni}</td><td style={styles.td}>{c.nombre}</td></tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

const styles = {
  page:  { padding:'32px', maxWidth:'800px', margin:'0 auto' },
  title: { color:'var(--color-heading)', marginBottom:'24px' },
  card:  { background:'var(--color-surface)', padding:'24px', borderRadius:'12px', boxShadow:'0 2px 10px var(--color-shadow)', marginBottom:'24px' },
  form:  { display:'flex', gap:'12px', flexWrap:'wrap', alignItems:'center' },
  input: { padding:'10px', border:'1px solid var(--color-border-strong)', borderRadius:'6px', flex:'1', minWidth:'140px' },
  btn:   { padding:'10px 20px', backgroundColor:'var(--color-primary)', color:'var(--color-on-primary)', border:'none', borderRadius:'6px', cursor:'pointer', fontWeight:'bold' },
  empty: { color:'var(--color-text-muted)' },
  table: { width:'100%', borderCollapse:'collapse', textAlign:'left', marginTop:'10px' },
  trHead: { borderBottom:'2px solid var(--color-border)' },
  tr:    { borderBottom:'1px solid var(--color-border)' },
  th:    { padding:'12px 8px', color:'var(--color-text-muted)' },
  td:    { padding:'12px 8px' },
};
