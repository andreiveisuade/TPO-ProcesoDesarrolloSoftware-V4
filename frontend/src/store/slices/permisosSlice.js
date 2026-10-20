import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { updatePermisosSupervisor, getUsuariosSupervisor } from '../../api/supervisor';
import { getUsuariosAdmin, updateRolUsuario, crearUsuarioAdmin, updateActivoUsuario } from '../../api/admin';

export const fetchUsuariosAdmin = createAsyncThunk('permisos/fetchAdmin', async (_, { rejectWithValue }) => {
  try {
    return await getUsuariosAdmin();
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const fetchUsuariosSupervisor = createAsyncThunk('permisos/fetchSupervisor', async (_, { rejectWithValue }) => {
  try {
    return await getUsuariosSupervisor();
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const actualizarPermisos = createAsyncThunk('permisos/actualizar', async ({ id, permisos }, { rejectWithValue }) => {
  try {
    return await updatePermisosSupervisor(id, permisos);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const cambiarRol = createAsyncThunk('permisos/cambiarRol', async ({ id, nuevoRol }, { rejectWithValue }) => {
  try {
    return await updateRolUsuario(id, nuevoRol);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const crearUsuario = createAsyncThunk('permisos/crearUsuario', async (usuario, { rejectWithValue }) => {
  try {
    return await crearUsuarioAdmin(usuario);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const cambiarActivo = createAsyncThunk('permisos/cambiarActivo', async ({ id, activo }, { rejectWithValue }) => {
  try {
    return await updateActivoUsuario(id, activo);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

const permisosSlice = createSlice({
  name: 'permisos',
  initialState: {
    lista: [],
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    const handlePending = (state) => { state.loading = true; state.error = null; };
    const handleFulfilled = (state, action) => { state.loading = false; state.lista = action.payload; };
    const handleRejected = (state, action) => { state.loading = false; state.error = action.payload; };
    const reemplazarUsuario = (state, action) => {
      const index = state.lista.findIndex(u => u.id === action.payload.id);
      if (index !== -1) state.lista[index] = action.payload;
    };

    builder
      .addCase(fetchUsuariosAdmin.pending, handlePending)
      .addCase(fetchUsuariosAdmin.fulfilled, handleFulfilled)
      .addCase(fetchUsuariosAdmin.rejected, handleRejected)
      
      .addCase(fetchUsuariosSupervisor.pending, handlePending)
      .addCase(fetchUsuariosSupervisor.fulfilled, handleFulfilled)
      .addCase(fetchUsuariosSupervisor.rejected, handleRejected)
      
      .addCase(actualizarPermisos.fulfilled, reemplazarUsuario)
      .addCase(cambiarRol.fulfilled, reemplazarUsuario)

      .addCase(crearUsuario.pending, handlePending)
      .addCase(crearUsuario.fulfilled, (state, action) => { state.loading = false; state.lista.push(action.payload); })
      .addCase(crearUsuario.rejected, handleRejected)

      .addCase(cambiarActivo.pending, handlePending)
      .addCase(cambiarActivo.fulfilled, (state, action) => { state.loading = false; reemplazarUsuario(state, action); })
      .addCase(cambiarActivo.rejected, handleRejected);
  },
});

export default permisosSlice.reducer;