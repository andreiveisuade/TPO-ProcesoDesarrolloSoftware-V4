import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../store/slices/authSlice';
import { ROLES } from '../utils/roles';

// Barra superior con el usuario logueado y los links que corresponden a su rol.
export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  
  const user = useSelector((state) => state.auth.user);

  const isAdmin = user?.rol === ROLES.ADMIN;
  const isSupervisor = user?.rol === ROLES.SUPERVISOR;

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <nav style={styles.nav}>
      <span style={styles.brand}>💳 Créditos UADE</span>
      {user && (
        <div style={styles.links}>
          <Link to="/clientes" style={styles.link}>Clientes</Link>
          <Link to="/creditos" style={styles.link}>Créditos</Link>
          <Link to="/cobranzas" style={styles.link}>Cobranzas</Link>
          
          {(isSupervisor || isAdmin) && (
            <Link to="/estadisticas" style={styles.supervisorLink}>Dashboard</Link>
          )}

          {isSupervisor && (
             <Link to="/supervisor/permisos-anulacion" style={styles.supervisorLink}>Gestor de Permisos</Link>
          )}

          {isAdmin && (
             <Link to="/admin/roles" style={styles.adminLink}>Panel Admin</Link>
          )}

          <span style={styles.user}>👤 {user.username} ({user.rol})</span>
          <button onClick={handleLogout} style={styles.btn}>Salir</button>
        </div>
      )}
    </nav>
  );
}

const styles = {
  nav: { display:'flex', justifyContent:'space-between', alignItems:'center', gap:'24px', padding:'12px 24px', backgroundColor:'var(--color-nav-bg)', color:'var(--color-on-primary)' },
  brand: { fontWeight:'bold', fontSize:'1.2rem', whiteSpace:'nowrap' },
  links: { display:'flex', flexWrap:'wrap', justifyContent:'flex-end', alignItems:'center', gap:'8px 16px', whiteSpace:'nowrap' },
  link: { color:'var(--color-nav-link)', textDecoration:'none', fontWeight:'500' },
  adminLink: { color: 'var(--color-nav-admin)', textDecoration:'none', fontWeight:'bold' }, 
  supervisorLink: { color: 'var(--color-nav-supervisor)', textDecoration:'none', fontWeight:'bold' }, 
  user: { color:'var(--color-nav-muted)', fontSize:'0.9rem' },
  btn: { background:'var(--color-danger-solid)', color:'var(--color-on-primary)', border:'none', padding:'6px 14px', borderRadius:'6px', cursor:'pointer' },
};