package com.ecommerce.audit.infrastructure.exception;

import com.ecommerce.audit.application.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para centralizar respuestas de error uniformes en REST.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 409 Conflict ──────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException ex, WebRequest request) {
        log.warn("Estado inválido en la solicitud: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "INVALID_STATE", ex.getMessage(), request);
    }

    // ── 400 Bad Request ───────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Parámetro requerido faltante: {}", ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                String.format("El parámetro requerido '%s' no fue enviado.", ex.getParameterName()), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Tipo de dato incorrecto en parámetro '{}': valor '{}'", ex.getName(), ex.getValue());
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER_TYPE",
                String.format("El valor '%s' no es válido para el parámetro '%s'.", ex.getValue(), ex.getName()), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> ApiErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .rejectedValue(fe.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        log.warn("Error de validación: {} campo(s) incorrecto(s)", fieldErrors.size());

        ApiErrorResponse body = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("La validación de la solicitud falló. Verifique la lista de fieldErrors.")
                .path(extractPath(request))
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── 500 Generic ───────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Error inesperado no controlado en el servidor", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error inesperado en el servidor. Por favor, intente más tarde.", request);
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String error,
                                                    String message, WebRequest request) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(extractPath(request))
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
