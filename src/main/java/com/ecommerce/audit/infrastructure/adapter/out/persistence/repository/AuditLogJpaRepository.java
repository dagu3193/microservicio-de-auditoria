package com.ecommerce.audit.infrastructure.adapter.out.persistence.repository;

import com.ecommerce.audit.infrastructure.adapter.out.persistence.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repositorio JPA para realizar consultas directas sobre la base de datos de auditoría.
 */
@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, UUID> {

    /**
     * Busca registros de auditoría filtrando de manera opcional por tipo de cambio, id del item,
     * responsable del cambio (búsqueda parcial) y rango de fechas.
     */
    @Query(value = """
            SELECT * FROM audit_logs a
            WHERE (:changeType IS NULL OR a.change_type = :changeType)
              AND (:itemId IS NULL OR a.item_id = :itemId)
              AND (:changedBy IS NULL OR LOWER(a.changed_by) LIKE LOWER(CONCAT('%', :changedBy, '%')))
              AND (a.changed_at BETWEEN :from AND :to)
            ORDER BY a.changed_at DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM audit_logs a
            WHERE (:changeType IS NULL OR a.change_type = :changeType)
              AND (:itemId IS NULL OR a.item_id = :itemId)
              AND (:changedBy IS NULL OR LOWER(a.changed_by) LIKE LOWER(CONCAT('%', :changedBy, '%')))
              AND (a.changed_at BETWEEN :from AND :to)
            """,
            nativeQuery = true)
    Page<AuditLogEntity> findByCriteria(
            @Param("changeType") String changeType,
            @Param("itemId") String itemId,
            @Param("changedBy") String changedBy,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
