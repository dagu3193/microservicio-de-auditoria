package com.ecommerce.audit.infrastructure.adapter.in.rest;

import com.ecommerce.audit.application.dto.RegisterAuditRequest;
import com.ecommerce.audit.domain.model.AuditType;
import com.ecommerce.audit.infrastructure.adapter.out.persistence.entity.AuditLogEntity;
import com.ecommerce.audit.infrastructure.adapter.out.persistence.repository.AuditLogJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración para el controlador REST de auditoría.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogJpaRepository jpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe registrar un log de auditoría vía POST y persistirlo en la base de datos")
    void shouldRegisterLogViaPost() throws Exception {
        RegisterAuditRequest request = RegisterAuditRequest.builder()
                .changeType(AuditType.PRICE)
                .itemId("EVT-999")
                .itemName("Fórmula 1 Bogotá")
                .oldValue("500.00")
                .newValue("650.00")
                .reason("Alta demanda y cambio de locación")
                .changedBy("admin-diego")
                .build();

        mockMvc.perform(post("/api/audit/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.changeType").value("PRICE"))
                .andExpect(jsonPath("$.itemId").value("EVT-999"))
                .andExpect(jsonPath("$.itemName").value("Fórmula 1 Bogotá"))
                .andExpect(jsonPath("$.newValue").value("650.00"));

        assertThat(jpaRepository.count()).isEqualTo(1);
        AuditLogEntity entity = jpaRepository.findAll().get(0);
        assertThat(entity.getItemId()).isEqualTo("EVT-999");
        assertThat(entity.getNewValue()).isEqualTo("650.00");
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request estructurado cuando el payload no es válido")
    void shouldReturn400ForInvalidPayload() throws Exception {
        RegisterAuditRequest request = RegisterAuditRequest.builder()
                .changeType(null) // inválido
                .itemId("") // inválido
                .itemName("Fórmula 1")
                .newValue("650.00")
                .changedBy("admin-diego")
                .build();

        mockMvc.perform(post("/api/audit/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(2)));
    }

    @Test
    @DisplayName("Debe filtrar y listar los logs de auditoría guardados vía GET")
    void shouldGetLogsWithFilters() throws Exception {
        AuditLogEntity log1 = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .changeType(AuditType.PRICE)
                .itemId("EVT-1")
                .itemName("Evento Uno")
                .newValue("100.00")
                .changedBy("admin-diego")
                .changedAt(LocalDateTime.now().minusHours(2))
                .build();

        AuditLogEntity log2 = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .changeType(AuditType.QUOTA)
                .itemId("EVT-2")
                .itemName("Evento Dos")
                .newValue("50")
                .changedBy("admin-juan")
                .changedAt(LocalDateTime.now().minusHours(1))
                .build();

        jpaRepository.save(log1);
        jpaRepository.save(log2);

        // Consulta sin filtros (deben retornar los 2)
        mockMvc.perform(get("/api/audit/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // Filtrado por tipo de cambio (debe retornar 1)
        mockMvc.perform(get("/api/audit/logs")
                        .param("changeType", "QUOTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].itemId").value("EVT-2"));

        // Filtrado parcial por administrador (debe retornar 1)
        mockMvc.perform(get("/api/audit/logs")
                        .param("changedBy", "diego"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].itemId").value("EVT-1"));
    }
}
