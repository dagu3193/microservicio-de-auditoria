package com.ecommerce.audit.application.service;

import com.ecommerce.audit.domain.model.AuditLog;
import com.ecommerce.audit.domain.model.AuditType;
import com.ecommerce.audit.domain.port.out.AuditRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditRepositoryPort repositoryPort;

    @InjectMocks
    private AuditLogService auditLogService;

    @Nested
    @DisplayName("Tests para el Dominio de AuditLog (Validaciones de Negocio)")
    class DomainTests {

        @Test
        @DisplayName("Debe crear correctamente una instancia válida de AuditLog")
        void shouldCreateValidAuditLog() {
            AuditLog auditLog = AuditLog.create(
                    AuditType.PRICE,
                    "EVT-123",
                    "Concierto Rock VIP",
                    "150.00",
                    "175.00",
                    "Ajuste por inflación",
                    "admin-diego"
            );

            assertThat(auditLog.getId()).isNotNull();
            assertThat(auditLog.getChangeType()).isEqualTo(AuditType.PRICE);
            assertThat(auditLog.getItemId()).isEqualTo("EVT-123");
            assertThat(auditLog.getItemName()).isEqualTo("Concierto Rock VIP");
            assertThat(auditLog.getOldValue()).isEqualTo("150.00");
            assertThat(auditLog.getNewValue()).isEqualTo("175.00");
            assertThat(auditLog.getReason()).isEqualTo("Ajuste por inflación");
            assertThat(auditLog.getChangedBy()).isEqualTo("admin-diego");
            assertThat(auditLog.getChangedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Debe fallar si falta el ID del ítem")
        void shouldFailWhenItemIdIsEmpty() {
            assertThatThrownBy(() -> AuditLog.create(
                    AuditType.PRICE,
                    "",
                    "Concierto Rock VIP",
                    "150.00",
                    "175.00",
                    "Ajuste por inflación",
                    "admin-diego"
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("El ID del ítem (itemId) es obligatorio");
        }

        @Test
        @DisplayName("Debe fallar si falta el nuevo valor")
        void shouldFailWhenNewValueIsEmpty() {
            assertThatThrownBy(() -> AuditLog.create(
                    AuditType.PRICE,
                    "EVT-123",
                    "Concierto Rock VIP",
                    "150.00",
                    "",
                    "Ajuste por inflación",
                    "admin-diego"
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("El nuevo valor (newValue) es obligatorio");
        }

        @Test
        @DisplayName("Debe fallar si falta el responsable del cambio")
        void shouldFailWhenChangedByIsEmpty() {
            assertThatThrownBy(() -> AuditLog.create(
                    AuditType.PRICE,
                    "EVT-123",
                    "Concierto Rock VIP",
                    "150.00",
                    "175.00",
                    "Ajuste por inflación",
                    null
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("El responsable del cambio (changedBy) es obligatorio");
        }
    }

    @Nested
    @DisplayName("Tests para el Servicio AuditLogService")
    class ServiceTests {

        @Test
        @DisplayName("Debe registrar un log correctamente delegando en el puerto de salida")
        void shouldRegisterLogSuccessfully() {
            AuditLog auditLog = AuditLog.create(
                    AuditType.PRICE,
                    "EVT-123",
                    "Concierto Rock VIP",
                    "150.00",
                    "175.00",
                    "Ajuste por inflación",
                    "admin-diego"
            );

            when(repositoryPort.save(any(AuditLog.class))).thenReturn(auditLog);

            AuditLog result = auditLogService.register(auditLog);

            assertThat(result).isNotNull();
            assertThat(result.getItemId()).isEqualTo("EVT-123");
            verify(repositoryPort, times(1)).save(auditLog);
        }

        @Test
        @DisplayName("Debe retornar un log por ID si existe")
        void shouldReturnLogById() {
            UUID id = UUID.randomUUID();
            AuditLog auditLog = AuditLog.builder()
                    .id(id)
                    .changeType(AuditType.QUOTA)
                    .itemId("EVT-123")
                    .itemName("Concierto Rock VIP")
                    .newValue("100")
                    .changedBy("admin-diego")
                    .build();

            when(repositoryPort.findById(id)).thenReturn(Optional.of(auditLog));

            Optional<AuditLog> result = auditLogService.getById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
            verify(repositoryPort, times(1)).findById(id);
        }

        @Test
        @DisplayName("Debe consultar logs paginados y filtrados")
        void shouldQueryLogsWithFilters() {
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .changeType(AuditType.QUOTA)
                    .itemId("EVT-123")
                    .itemName("Concierto Rock VIP")
                    .newValue("100")
                    .changedBy("admin-diego")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog));

            when(repositoryPort.findByCriteria(any(), any(), any(), any(), any(), any()))
                    .thenReturn(page);

            Page<AuditLog> result = auditLogService.queryLogs(
                    AuditType.QUOTA,
                    "EVT-123",
                    "admin-diego",
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now(),
                    pageable
            );

            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0).getItemId()).isEqualTo("EVT-123");
            verify(repositoryPort, times(1))
                    .findByCriteria(any(), any(), any(), any(), any(), any());
        }
    }
}
