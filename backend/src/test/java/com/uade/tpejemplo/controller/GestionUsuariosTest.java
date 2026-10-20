package com.uade.tpejemplo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpejemplo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Alta por el ADMIN (Facade) y baja logica (Adapter: isEnabled) de punta a punta, con los usuarios semilla.
@SpringBootTest
@AutoConfigureMockMvc
class GestionUsuariosTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void adminDaDeAltaUnSupervisor() throws Exception {
        mvc.perform(alta("alta-sup", "SUPERVISOR"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.rol").value("SUPERVISOR"))
            .andExpect(jsonPath("$.activo").value(true));

        login("alta-sup", "secreto").andExpect(status().isOk());
    }

    @Test
    void altaConRolAdminDa400() throws Exception {
        mvc.perform(alta("alta-admin", "ADMIN")).andExpect(status().isBadRequest());
    }

    @Test
    void usuarioDesactivadoNoPuedeLoguearse() throws Exception {
        Long id = crear("baja-login");

        mvc.perform(activo(id, false)).andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));

        login("baja-login", "secreto")
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.mensajes[0]").value("El usuario está desactivado"));
    }

    @Test
    void tokenEmitidoAntesDeDesactivarDejaDeServir() throws Exception {
        Long id = crear("baja-token");
        String token = token("baja-token", "secreto");
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        mvc.perform(activo(id, false)).andExpect(status().isOk());

        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioReactivadoVuelveAEntrar() throws Exception {
        Long id = crear("baja-vuelta");
        mvc.perform(activo(id, false)).andExpect(status().isOk());

        mvc.perform(activo(id, true)).andExpect(status().isOk());

        login("baja-vuelta", "secreto").andExpect(status().isOk());
    }

    @Test
    void desactivarUnAdminDa400() throws Exception {
        Long idAdmin = usuarioRepository.findByUsername("admin").orElseThrow().getId();

        mvc.perform(activo(idAdmin, false)).andExpect(status().isBadRequest());
    }

    private Long crear(String username) throws Exception {
        String cuerpo = mvc.perform(alta(username, "USER")).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo).get("id").asLong();
    }

    private RequestBuilder alta(String username, String rol) throws Exception {
        return post("/api/admin/usuarios").header("Authorization", "Bearer " + token("admin", "admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"secreto\",\"rol\":\"" + rol + "\"}");
    }

    private RequestBuilder activo(Long id, boolean activo) throws Exception {
        return put("/api/admin/usuarios/" + id + "/activo").header("Authorization", "Bearer " + token("admin", "admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"activo\":" + activo + "}");
    }

    private ResultActions login(String username, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"));
    }

    private String token(String username, String password) throws Exception {
        JsonNode respuesta = json.readTree(login(username, password).andReturn().getResponse().getContentAsString());
        return respuesta.get("token").asText();
    }
}
