package com.ecommerce.audit.application.dto;

import com.ecommerce.audit.domain.model.AuditType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para registrar manualmente un nuevo log de auditoría.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAuditRequest {

    @NotNull(message = "El tipo de cambio (changeType) es obligatorio y debe ser PRICE o QUOTA")
    private AuditType changeType;

    @NotBlank(message = "El ID del ítem (itemId) es obligatorio")
    private String itemId;

    @NotBlank(message = "El nombre del ítem (itemName) es obligatorio")
    private String itemName;

    private String oldValue;

    @NotBlank(message = "El nuevo valor (newValue) es obligatorio")
    private String newValue;

    private String reason;

    @NotBlank(message = "El responsable del cambio (changedBy) es obligatorio")
    private String changedBy;
}
