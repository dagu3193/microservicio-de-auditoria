package com.ecommerce.audit.infrastructure.adapter.out.persistence.entity;

import com.ecommerce.audit.domain.model.AuditType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la persistencia del log de auditoría.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_type", columnList = "change_type"),
        @Index(name = "idx_audit_logs_item", columnList = "item_id"),
        @Index(name = "idx_audit_logs_changed_by", columnList = "changed_by"),
        @Index(name = "idx_audit_logs_changed_at", columnList = "changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 30)
    private AuditType changeType;

    @Column(name = "item_id", nullable = false, length = 100)
    private String itemId;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "old_value", length = 200)
    private String oldValue;

    @Column(name = "new_value", nullable = false, length = 200)
    private String newValue;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
