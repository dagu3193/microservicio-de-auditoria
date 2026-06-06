package com.ecommerce.audit.infrastructure.adapter.in.messaging;

import com.ecommerce.audit.domain.model.AuditLog;
import com.ecommerce.audit.domain.model.AuditType;
import com.ecommerce.audit.domain.port.in.RegisterAuditUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adaptador de entrada para mensajería asíncrona vía RabbitMQ.
 * Escucha la cola de cambios y registra auditorías.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceQuotaEventConsumer {

    private final RegisterAuditUseCase registerUseCase;

    /**
     * Consume eventos de cambios de precio y cupos de forma dinámica.
     * Inspecciona la routing key para determinar el tipo de cambio.
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.price-quota-audit}")
    public void consumeEvent(Map<String, Object> payload, @Header("amqp_receivedRoutingKey") String routingKey) {
        log.info("Mensaje asíncrono recibido de RabbitMQ. RoutingKey: {}", routingKey);
        
        try {
            AuditType type;
            String oldValue = null;
            String newValue = null;

            if (routingKey.contains("price")) {
                type = AuditType.PRICE;
                oldValue = payload.get("oldPrice") != null ? payload.get("oldPrice").toString() : null;
                newValue = payload.get("newPrice") != null ? payload.get("newPrice").toString() : null;
            } else if (routingKey.contains("quota")) {
                type = AuditType.QUOTA;
                oldValue = payload.get("oldQuota") != null ? payload.get("oldQuota").toString() : null;
                newValue = payload.get("newQuota") != null ? payload.get("newQuota").toString() : null;
            } else {
                log.warn("La RoutingKey '{}' no es manejada por este consumidor. Se descarta el mensaje.", routingKey);
                return;
            }

            String itemId = payload.get("itemId") != null ? payload.get("itemId").toString() : null;
            String itemName = payload.get("itemName") != null ? payload.get("itemName").toString() : null;
            String changedBy = payload.get("changedBy") != null ? payload.get("changedBy").toString() : "SYSTEM";
            String reason = payload.get("reason") != null ? payload.get("reason").toString() : "Actualización asíncrona de RabbitMQ";

            // Validación de campos mínimos obligatorios para evitar fallos de negocio
            if (itemId == null || itemName == null || newValue == null) {
                log.warn("El evento recibido no contiene todos los campos mínimos obligatorios (itemId, itemName, newValue). Payload: {}", payload);
                return;
            }

            AuditLog auditLog = AuditLog.create(type, itemId, itemName, oldValue, newValue, reason, changedBy);
            registerUseCase.register(auditLog);
            
            log.info("Registro de auditoría guardado exitosamente desde evento asíncrono. Tipo: {}", type);
        } catch (Exception e) {
            log.error("Error procesando mensaje asíncrono de RabbitMQ", e);
        }
    }
}
