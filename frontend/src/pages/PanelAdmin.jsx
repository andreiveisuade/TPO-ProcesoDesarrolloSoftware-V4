import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUsuariosAdmin, cambiarRol, crearUsuario, cambiarActivo } from '../store/slices/permisosSlice';
import { ROLES } from '../utils/roles';

const FORM_VACIO = { username: '', password: '', rol: ROLES.USER };

// Panel del ADMIN para dar de alta usuarios, cambiar su rol y desactivarlos.
const PanelAdmin = () => {
  const dispatch = useDispatch();
  const { lista: usuarios, loading, error } = useSelector((state) => state.permisos);
  const [form, setForm] = useState(FORM_VACIO);

  useEffect(() => {
    dispatch(fetchUsuariosAdmin());
  }, [dispatch]);

  const handleRoleChange = (usuarioId, nuevoRol) => {
    dispatch(cambiarRol({ id: usuarioId, nuevoRol }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(crearUsuario(form));
    if (result.meta.requestStatus === 'fulfilled') setForm(FORM_VACIO);
  };

  return (
    <div style={styles.page}>
      <h2 style={styles.title}>
        Gestión de Usuarios <span style={styles.adminBadge}>(Panel Admin)</span>
      </h2>
      
      {error && <div style={styles.error}>Error: {error}</div>}

      <div style={styles.card}>
        <h3 style={styles.subtitle}>Nuevo usuario</h3>
        <form onSubmit={handleSubmit} style={styles.form}>
          <input style={styles.input} placeholder="Usuario" value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} required />
          <input style={styles.input} type="password" placeholder="Contraseña" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} required minLength={6} />
          <select style={styles.select} value={form.rol} onChange={e => setForm({ ...form, rol: e.target.value })}>
            <option value={ROLES.USER}>{ROLES.USER}</option>
            <option value={ROLES.SUPERVISOR}>{ROLES.SUPERVISOR}</option>
          </select>
          <button style={styles.btn} disabled={loading}>Crear</button>
        </form>
      </div>

      <div style={styles.card}>
        {loading && usuarios.length === 0 ? (
          <p style={styles.loading}>Cargando lista de usuarios...</p>
        ) : (
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Usuario</th>
                <th style={styles.th}>Rol Actual</th>
                <th style={styles.thCenter}>Asignar Nuevo Rol</th>
                <th style={styles.thCenter}>Estado</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map(u => (
                <tr key={u.id} style={styles.tr}>
                  <td style={styles.td}>{u.username}</td>
                  <td style={styles.td}>
                    <span style={u.rol === ROLES.SUPERVISOR ? styles.tagSupervisor : styles.tagUser}>
                      {u.rol}
                    </span>
                  </td>
                  <td style={styles.tdCenter}>
                    <select 
                      value={u.rol} 
                      onChange={(e) => handleRoleChange(u.id, e.target.value)}
                      disabled={loading}
                      style={styles.select}
                    >
                      <option value={ROLES.USER}>{ROLES.USER}</option>
                      <option value={ROLES.SUPERVISOR}>{ROLES.SUPERVISOR}</option>
                    </select>
                  </td>
                  <td style={styles.tdCenter}>
                    <span style={u.activo ? styles.tagActivo : styles.tagInactivo}>{u.activo ? 'Activo' : 'Inactivo'}</span>
                    {u.rol !== ROLES.ADMIN && (
                      <button
                        style={styles.btnEstado}
                        onClick={() => dispatch(cambiarActivo({ id: u.id, activo: !u.activo }))}
                        disabled={loading}
                      >
                        {u.activo ? 'Desactivar' : 'Reactivar'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

const styles = {
  page: { padding: '20px', fontFamily: 'Arial, sans-serif', maxWidth: '800px', margin: '0 auto' },
  title: { color: 'var(--color-text)', borderBottom: '2px solid var(--color-border)', paddingBottom: '10px' },
  card: { background: 'var(--color-surface)', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px var(--color-shadow)', marginBottom: '20px' },
  subtitle: { marginTop: 0, color: 'var(--color-text)' },
  form: { display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' },
  input: { padding: '8px', border: '1px solid var(--color-border-strong)', borderRadius: '4px', flex: '1', minWidth: '140px' },
  btn: { padding: '8px 20px', backgroundColor: 'var(--color-primary)', color: 'var(--color-on-primary)', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' },
  btnEstado: { marginLeft: '10px', padding: '4px 10px', borderRadius: '4px', border: '1px solid var(--color-border-strong)', background: 'var(--color-surface-alt)', color: 'var(--color-text)', cursor: 'pointer' },
  tagActivo: { backgroundColor: 'var(--color-success-bg)', color: 'var(--color-success)', padding: '4px 8px', borderRadius: '4px', fontSize: '0.85rem' },
  tagInactivo: { backgroundColor: 'var(--color-danger-bg)', color: 'var(--color-danger)', padding: '4px 8px', borderRadius: '4px', fontSize: '0.85rem' },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: { padding: '12px', textAlign: 'left', borderBottom: '2px solid var(--color-border)', color: 'var(--color-text-muted)' },
  thCenter: { padding: '12px', textAlign: 'center', borderBottom: '2px solid var(--color-border)', color: 'var(--color-text-muted)' },
  tr: { borderBottom: '1px solid var(--color-border)' },
  td: { padding: '12px', color: 'var(--color-text)' },
  tdCenter: { padding: '12px', textAlign: 'center' },
  select: { padding: '6px 12px', borderRadius: '4px', border: '1px solid var(--color-border-strong)', cursor: 'pointer' },
  error: { color: 'var(--color-on-primary)', backgroundColor: 'var(--color-danger-solid)', padding: '10px', borderRadius: '4px', marginBottom: '10px' },
  loading: { color: 'var(--color-link)', textAlign: 'center', fontWeight: 'bold' },
  adminBadge: { fontSize: '0.5em', color: 'var(--color-on-primary)', backgroundColor: 'var(--color-warning-solid)', padding: '4px 8px', borderRadius: '12px', verticalAlign: 'middle', marginLeft: '10px' },
  tagUser: { backgroundColor: 'var(--color-surface-alt)', padding: '4px 8px', borderRadius: '4px', fontSize: '0.85rem' },
  tagSupervisor: { backgroundColor: 'var(--color-accent-bg)', color: 'var(--color-accent)', padding: '4px 8px', borderRadius: '4px', fontSize: '0.85rem', fontWeight: 'bold' }
};

export default PanelAdmin;