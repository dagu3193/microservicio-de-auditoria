package com.ecommerce.audit.domain.port.in;

import com.ecommerce.audit.domain.model.AuditLog;

/**
 * Puerto de entrada para registrar logs de auditoría.
 */
public interface RegisterAuditUseCase {
    AuditLog register(AuditLog auditLog);
}
