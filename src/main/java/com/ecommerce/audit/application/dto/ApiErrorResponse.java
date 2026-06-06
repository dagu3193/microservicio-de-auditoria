package com.ecommerce.audit.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta estándar de error de API.
 * Todos los errores de la API retornan este formato para consistencia con los demás microservicios.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de error estándar de la API")
public class ApiErrorResponse {

    @Schema(description = "Código HTTP de estado", example = "400")
    private final int status;

    @Schema(description = "Código de error corto", example = "VALIDATION_ERROR")
    private final String error;

    @Schema(description = "Mensaje de error legible", example = "El ID del item es obligatorio")
    private final String message;

    @Schema(description = "Ruta del endpoint que causó el error", example = "/api/audit/logs")
    private final String path;

    @Schema(description = "Timestamp del error")
    private final LocalDateTime timestamp;

    @Schema(description = "Errores de validación por campo (solo para respuestas 400)")
    private final List<FieldError> fieldErrors;

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }
}
