package com.ecommerce.audit.domain.port.out;

import com.ecommerce.audit.domain.model.AuditLog;
import com.ecommerce.audit.domain.model.AuditType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de los logs de auditoría.
 */
public interface AuditRepositoryPort {

    AuditLog save(AuditLog auditLog);

    Optional<AuditLog> findById(UUID id);

    Page<AuditLog> findByCriteria(AuditType changeType, String itemId, String changedBy,
                                  LocalDateTime from, LocalDateTime to, Pageable pageable);
}
