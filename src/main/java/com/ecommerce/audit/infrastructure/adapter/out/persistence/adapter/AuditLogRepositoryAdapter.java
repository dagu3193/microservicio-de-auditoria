package com.ecommerce.audit.infrastructure.adapter.out.persistence.adapter;

import com.ecommerce.audit.domain.model.AuditLog;
import com.ecommerce.audit.domain.model.AuditType;
import com.ecommerce.audit.domain.port.out.AuditRepositoryPort;
import com.ecommerce.audit.infrastructure.adapter.out.persistence.entity.AuditLogEntity;
import com.ecommerce.audit.infrastructure.adapter.out.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida para persistencia de logs de auditoría.
 * Implementa {@link AuditRepositoryPort} delegando en {@link AuditLogJpaRepository}.
 */
@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditRepositoryPort {

    private final AuditLogJpaRepository jpaRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = toEntity(auditLog);
        AuditLogEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AuditLog> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<AuditLog> findByCriteria(AuditType changeType, String itemId, String changedBy,
                                         LocalDateTime from, LocalDateTime to, Pageable pageable) {
        // Proveer valores por defecto en caso de parámetros nulos en fechas
        LocalDateTime finalFrom = from != null ? from : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime finalTo = to != null ? to : LocalDateTime.now().plusYears(10); // futuro lejano

        return jpaRepository.findByCriteria(changeType, itemId, changedBy, finalFrom, finalTo, pageable)
                .map(this::toDomain);
    }

    // ── Mappers Manuales (Clean Code, sin sobrecarga de dependencias) ─────────

    private AuditLogEntity toEntity(AuditLog domain) {
        if (domain == null) return null;
        return AuditLogEntity.builder()
                .id(domain.getId())
                .changeType(domain.getChangeType())
                .itemId(domain.getItemId())
                .itemName(domain.getItemName())
                .oldValue(domain.getOldValue())
                .newValue(domain.getNewValue())
                .reason(domain.getReason())
                .changedBy(domain.getChangedBy())
                .changedAt(domain.getChangedAt())
                .build();
    }

    private AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) return null;
        return AuditLog.builder()
                .id(entity.getId())
                .changeType(entity.getChangeType())
                .itemId(entity.getItemId())
                .itemName(entity.getItemName())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .reason(entity.getReason())
                .changedBy(entity.getChangedBy())
                .changedAt(entity.getChangedAt())
                .build();
    }
}
