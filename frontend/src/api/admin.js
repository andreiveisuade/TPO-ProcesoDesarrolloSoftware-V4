import { api } from './apiClient';

export const getUsuariosAdmin = () => api.get('/admin/usuarios');
export const updateRolUsuario = (id, nuevoRol) => api.put(`/admin/usuarios/${id}/rol`, { rol: nuevoRol });
export const crearUsuarioAdmin = (usuario) => api.post('/admin/usuarios', usuario);
export const updateActivoUsuario = (id, activo) => api.put(`/admin/usuarios/${id}/activo`, { activo });
