package com.ecommerce.audit.infrastructure.adapter.in.rest;

import com.ecommerce.audit.application.dto.AuditLogDto;
import com.ecommerce.audit.application.dto.RegisterAuditRequest;
import com.ecommerce.audit.domain.model.AuditLog;
import com.ecommerce.audit.domain.model.AuditType;
import com.ecommerce.audit.domain.port.in.QueryAuditUseCase;
import com.ecommerce.audit.domain.port.in.RegisterAuditUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Adaptador de entrada REST para exponer las APIs públicas de auditoría.
 */
@RestController
@RequestMapping("/api/audit/logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Endpoints para la auditoría de cambios en precios y cupos/capacidad")
public class AuditLogController {

    private final RegisterAuditUseCase registerUseCase;
    private final QueryAuditUseCase queryUseCase;

    /**
     * Registra manualmente una auditoría.
     */
    @PostMapping
    @Operation(summary = "Registrar un cambio", description = "Persiste un nuevo registro de auditoría de cambio en precio o cupo")
    public ResponseEntity<AuditLogDto> register(@Valid @RequestBody RegisterAuditRequest request) {
        AuditLog domainModel = AuditLog.create(
                request.getChangeType(),
                request.getItemId(),
                request.getItemName(),
                request.getOldValue(),
                request.getNewValue(),
                request.getReason(),
                request.getChangedBy()
        );
        AuditLog saved = registerUseCase.register(domainModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    /**
     * Consulta registros paginados y filtrados.
     */
    @GetMapping
    @Operation(summary = "Consultar registros", description = "Retorna un listado paginado filtrando opcionalmente por tipo, item, responsable y fechas")
    public ResponseEntity<Page<AuditLogDto>> query(
            @RequestParam(required = false) AuditType changeType,
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) String changedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> domainPage = queryUseCase.queryLogs(changeType, itemId, changedBy, from, to, pageable);
        return ResponseEntity.ok(domainPage.map(this::toDto));
    }

    /**
     * Consulta un registro por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener por ID", description = "Retorna el detalle completo de un log de auditoría por su ID único")
    public ResponseEntity<AuditLogDto> getById(@PathVariable UUID id) {
        return queryUseCase.getById(id)
                .map(domain -> ResponseEntity.ok(toDto(domain)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Mappers Manuales (Clean Code) ─────────────────────────────────────────

    private AuditLogDto toDto(AuditLog domain) {
        if (domain == null) return null;
        return AuditLogDto.builder()
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
}
