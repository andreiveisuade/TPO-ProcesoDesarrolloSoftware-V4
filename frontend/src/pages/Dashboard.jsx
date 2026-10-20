import { formatMoneda } from '../utils/formato';
import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchEstadisticas } from '../store/slices/dashboardSlice'; 
import { ROLES } from '../utils/roles';

// Estadisticas generales para SUPERVISOR y ADMIN.
const Dashboard = () => {
  const dispatch = useDispatch();
  
  const { data: estadisticas, loading, error } = useSelector((state) => state.dashboard);
  const { user } = useSelector((state) => state.auth);
  
  const puedeVerDashboard = user?.rol === ROLES.SUPERVISOR || user?.rol === ROLES.ADMIN;

  useEffect(() => {
    if (puedeVerDashboard) {
      dispatch(fetchEstadisticas());
    }
  }, [dispatch, puedeVerDashboard]);

  if (!puedeVerDashboard) return <div style={styles.center}>No tienes permisos para ver el dashboard.</div>;
  if (loading) return <div style={styles.center}>Cargando métricas del sistema...</div>;
  if (error) return <div style={styles.center}>Error: {error}</div>;

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>
        Panel de Estadísticas 
        <span style={styles.supervisorBadge}>(Modo {user?.rol === ROLES.ADMIN ? 'Admin' : 'Supervisor'})</span>
      </h2>
      
      <div style={styles.tarjetasMetricas}>
        <div style={styles.tarjeta}>
           <h3>Total Clientes</h3>
           <p style={styles.valor}>{estadisticas?.cantidadClientes || 0}</p>
        </div>
        <div style={styles.tarjeta}>
           <h3>Créditos Activos</h3>
           <p style={styles.valor}>{estadisticas?.cantidadCreditos || 0}</p>
        </div>
        <div style={styles.tarjeta}>
           <h3>Monto Total Financiado</h3>
           <p style={styles.valor}>{formatMoneda(estadisticas?.montoTotalFinanciado)}</p>
        </div>
        <div style={styles.tarjeta}>
           <h3>Monto Total Cobrado</h3>
           <p style={styles.valor}>{formatMoneda(estadisticas?.montoTotalCobrado)}</p>
        </div>
        <div style={styles.tarjeta}>
           <h3>Saldo Pendiente</h3>
           <p style={styles.valor}>{formatMoneda(estadisticas?.saldoPendiente)}</p>
        </div>
        <div style={styles.tarjeta}>
           <h3>Monto Vencido</h3>
           <p style={styles.valor}>{formatMoneda(estadisticas?.montoVencido)}</p>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: { padding:'32px', maxWidth:'900px', margin:'0 auto' },
  title:     { color:'var(--color-heading)', marginBottom:'24px', borderBottom: '2px solid var(--color-border)', paddingBottom: '10px' },
  center: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    fontSize: '1.2rem'
  },
  tarjetasMetricas: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(min(240px, 100%), 1fr))',
    gap: '20px',
  },
  tarjeta: {
    backgroundColor: 'var(--color-surface)',
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 4px 6px var(--color-shadow)',
    textAlign: 'center',
  },
  valor: {
    fontSize: '2rem',
    fontWeight: 'bold',
    color: 'var(--color-link)',
    margin: '10px 0 0 0',
  },
  supervisorBadge: {
    fontSize: '0.5em',
    color: 'var(--color-on-primary)',
    backgroundColor: 'var(--color-accent-solid)',
    padding: '4px 8px',
    borderRadius: '12px',
    verticalAlign: 'middle',
    marginLeft: '10px'
  }
};

export default Dashboard;