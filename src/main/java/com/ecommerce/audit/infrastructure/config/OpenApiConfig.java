package com.ecommerce.audit.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI y Swagger UI para el microservicio de auditoría.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private String serverPort;

    @Bean
    public OpenAPI auditServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Audit Service API")
                        .description("""
                                Microservicio de auditoría de cambios en precios y cupos/capacidad para la resolución de reclamos.
                                Construido bajo principios de arquitectura hexagonal y clean code.
                                
                                **Módulos y Endpoints disponibles:**
                                
                                📋 **Registro de Auditoría** — `/api/audit/logs`
                                - `POST /` — Registrar manualmente un cambio en precio o cupo.
                                - `GET /` — Consultar y filtrar logs con paginación (por tipo de cambio, id del item, administrador responsable y rango de fechas).
                                - `GET /{id}` — Consultar detalle de un registro específico.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Audit Service Team")
                                .email("audit@ecommerce.com"))
                        .license(new License().name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local"),
                        new Server().url("http://audit-service:8084").description("Docker")
                ));
    }
}
