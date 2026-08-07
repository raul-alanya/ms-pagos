package ms_pagos.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * API-IZI-001: Handler global de excepciones.
 * Todo error 4xx/5xx devuelve JSON uniforme:
 *   { "success": false, "message": "...", "errores": {...} }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Errores de @Valid / @Validated
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        return buildError(HttpStatus.BAD_REQUEST, "Error de validación", errores);
    }

    // ID con tipo incorrecto: /api/empresas/abc, /api/pagos/1.5, overflow int64
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String tipo = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        String msg = "El parámetro '" + ex.getName() + "' debe ser de tipo " + tipo
                + ". Valor recibido: '" + ex.getValue() + "'";
        return buildError(HttpStatus.BAD_REQUEST, msg, null);
    }

    // Body JSON malformado o tipos incompatibles en campos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(
            HttpMessageNotReadableException ex) {
        return buildError(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud tiene formato inválido o contiene tipos incompatibles",
                null);
    }

    // Conflictos de negocio: RUC/email duplicado, referencia duplicada, múltiples activas
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // Validaciones manuales: Luhn inválido, tarjeta vencida, caja inexistente, HTTPS requerido
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // RuntimeException genérica — distingue "no encontrado" (404) del resto (400)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Error inesperado";
        if (msg.toLowerCase().contains("no encontrad") || msg.toLowerCase().contains("not found")) {
            return buildError(HttpStatus.NOT_FOUND, msg, null);
        }
        return buildError(HttpStatus.BAD_REQUEST, msg, null);
    }

    // Error interno inesperado — no exponer detalles al cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Contacte al administrador.", null);
    }

    // ─── Helper ──────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message, Map<String, String> errores) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message != null ? message : "Error desconocido");
        if (errores != null && !errores.isEmpty()) {
            body.put("errores", errores);
        }
        return ResponseEntity.status(status).body(body);
    }
}
