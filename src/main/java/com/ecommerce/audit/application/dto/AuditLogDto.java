package com.ecommerce.audit.application.dto;

import com.ecommerce.audit.domain.model.AuditType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representación de transferencia de datos de un registro de auditoría.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private UUID id;
    private AuditType changeType;
    private String itemId;
    private String itemName;
    private String oldValue;
    private String newValue;
    private String reason;
    private String changedBy;
    private LocalDateTime changedAt;
}
