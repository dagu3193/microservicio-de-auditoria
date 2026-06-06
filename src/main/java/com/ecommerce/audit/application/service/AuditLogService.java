package com.ecommerce.audit.application.service;

import com.ecommerce.audit.domain.model.AuditLog;
import com.ecommerce.audit.domain.model.AuditType;
import com.ecommerce.audit.domain.port.in.QueryAuditUseCase;
import com.ecommerce.audit.domain.port.in.RegisterAuditUseCase;
import com.ecommerce.audit.domain.port.out.AuditRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestador de la lógica de negocio para auditoría.
 * Implementa los casos de uso (puertos de entrada).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService implements RegisterAuditUseCase, QueryAuditUseCase {

    private final AuditRepositoryPort repositoryPort;

    @Override
    @Transactional
    public AuditLog register(AuditLog auditLog) {
        log.info("Registrando log de auditoría: tipo={}, itemId={}, itemName={}, nuevoValor={}",
                auditLog.getChangeType(), auditLog.getItemId(), auditLog.getItemName(), auditLog.getNewValue());
        return repositoryPort.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLog> getById(UUID id) {
        log.debug("Consultando log de auditoría por ID: {}", id);
        return repositoryPort.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> queryLogs(AuditType changeType, String itemId, String changedBy,
                                    LocalDateTime from, LocalDateTime to, Pageable pageable) {
        log.debug("Consultando logs con criterios: changeType={}, itemId={}, changedBy={}, from={}, to={}",
                changeType, itemId, changedBy, from, to);
        return repositoryPort.findByCriteria(changeType, itemId, changedBy, from, to, pageable);
    }
}
