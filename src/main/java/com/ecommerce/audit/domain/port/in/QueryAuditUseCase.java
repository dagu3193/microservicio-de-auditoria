package com.ecommerce.audit.domain.port.in;

import com.ecommerce.audit.domain.model.AuditLog;
import com.ecommerce.audit.domain.model.AuditType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para realizar consultas y filtros sobre los logs de auditoría.
 */
public interface QueryAuditUseCase {
    
    Optional<AuditLog> getById(UUID id);

    Page<AuditLog> queryLogs(AuditType changeType, String itemId, String changedBy,
                             LocalDateTime from, LocalDateTime to, Pageable pageable);
}
