package com.ecommerce.audit.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditLog — Rich Domain Entity.
 *
 * Registra y valida los cambios en precios y cupos del e-commerce.
 */
@Getter
@ToString
@Builder(toBuilder = true)
public class AuditLog {

    private final UUID id;
    private final AuditType changeType;
    private final String itemId;
    private final String itemName;
    private final String oldValue;
    private final String newValue;
    private final String reason;
    private final String changedBy;
    private final LocalDateTime changedAt;

    // ===== FACTORY =====

    /**
     * Crea un nuevo registro de auditoria validado.
     */
    public static AuditLog create(AuditType changeType, String itemId, String itemName,
                                  String oldValue, String newValue, String reason, String changedBy) {
        if (changeType == null) {
            throw new IllegalArgumentException("El tipo de cambio (changeType) es obligatorio");
        }
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del ítem (itemId) es obligatorio");
        }
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del ítem (itemName) es obligatorio");
        }
        if (newValue == null || newValue.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo valor (newValue) es obligatorio");
        }
        if (changedBy == null || changedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("El responsable del cambio (changedBy) es obligatorio");
        }

        return AuditLog.builder()
                .id(UUID.randomUUID())
                .changeType(changeType)
                .itemId(itemId.trim())
                .itemName(itemName.trim())
                .oldValue(oldValue != null ? oldValue.trim() : null)
                .newValue(newValue.trim())
                .reason(reason != null ? reason.trim() : null)
                .changedBy(changedBy.trim())
                .changedAt(LocalDateTime.now())
                .build();
    }
}
