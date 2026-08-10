package ms_pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ms_pagos.dto.ApiResponseDTO;
import ms_pagos.dto.PageResponseDTO;
import ms_pagos.dto.PagoAdminDTO;
import ms_pagos.dto.PagoRechazoDTO;
import ms_pagos.dto.PagoRequestDTO;
import ms_pagos.dto.PagoResponseDTO;
import ms_pagos.dto.PagoResponseResultDTO;
import ms_pagos.dto.PagoSafeDTO;
import ms_pagos.entity.PagoRechazo;
import ms_pagos.entity.PagoResponse;
import ms_pagos.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos Online", description = "Endpoints para gestionar pagos online, respuestas y rechazos IZIPAY")
public class PagoController {

    // PERF-IZI-001: tamaño máximo de página para evitar respuestas sin límite
    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private PagoService pagoService;

    // ─── Helper: construye headers con Cache-Control no-store ────────────────
    private HttpHeaders noCacheHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate");
        headers.set("Pragma", "no-cache");
        return headers;
    }

    // ─── PAGO REQUEST ────────────────────────────────────────────────────────

    @Operation(summary = "Registrar datos de envío de pago online",
        description = "Valida PAN (Luhn), vigencia de tarjeta, unicidad de referencia y caja abierta. " +
                      "Devuelve PAN enmascarado. CVV nunca se expone en la respuesta.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del pago inválidos"),
        @ApiResponse(responseCode = "409", description = "Referencia duplicada")
    })
    @PostMapping("/procesar")
    public ResponseEntity<ApiResponseDTO<PagoSafeDTO>> registrarPago(
            @Valid @RequestBody PagoRequestDTO dto) {
        PagoSafeDTO pago = pagoService.registrarPago(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Datos de pago registrados exitosamente", pago));
    }

    @Operation(summary = "Listar pagos para el panel administrador",
        description = "Retorna todos los pagos (aprobados, rechazados y pendientes) con su sumatoria. " +
                      "Filtros opcionales: estado, referencia, cajaId, desde, hasta. " +
                      "Paginación opcional con page y size (máx 50); si no se envían, retorna todos.")
    @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida exitosamente")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> listarPagos(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamaño de página (máximo 50)", example = "10")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Filtrar por estado: APROBADO, RECHAZADO o PENDIENTE", example = "APROBADO")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Filtrar por referencia (búsqueda parcial)", example = "ORD-1786239304703")
            @RequestParam(required = false) String referencia,
            @Parameter(description = "Filtrar por ID de caja", example = "1")
            @RequestParam(required = false) Long cajaId,
            @Parameter(description = "Fecha inicial (yyyy-MM-dd'T'HH:mm:ss)", example = "2026-08-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha final (yyyy-MM-dd'T'HH:mm:ss)", example = "2026-08-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        List<PagoAdminDTO> filtrados = pagoService.listarPagosAdmin(estado, referencia, cajaId, desde, hasta);

        List<PagoAdminDTO> contenido = filtrados;
        if (page != null || size != null) {
            int pag = page != null ? Math.max(page, 0) : 0;
            int tam = size != null ? Math.min(Math.max(size, 1), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
            int inicio = Math.min(pag * tam, filtrados.size());
            int fin = Math.min(inicio + tam, filtrados.size());
            contenido = new ArrayList<>(filtrados.subList(inicio, fin));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("contenido", contenido);
        data.put("resumen", pagoService.resumenPagos(filtrados));
        return ResponseEntity.ok()
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Pagos obtenidos exitosamente", data));
    }

    @Operation(summary = "Obtener pago por ID",
        description = "Retorna el pago con los campos de consulta del panel administrador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pago encontrado"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PagoAdminDTO>> obtenerPago(
            @Parameter(description = "ID del pago", example = "38") @PathVariable Long id) {
        return pagoService.obtenerPagoAdminPorId(id)
                .map(p -> ResponseEntity.ok()
                        .headers(noCacheHeaders())
                        .<ApiResponseDTO<PagoAdminDTO>>body(ApiResponseDTO.ok("Pago encontrado", p)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Pago no encontrado con ID: " + id)));
    }

    @Operation(summary = "Buscar pagos por referencia",
        description = "Retorna pagos con los campos de consulta del panel administrador")
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    @GetMapping("/referencia/{reference}")
    public ResponseEntity<ApiResponseDTO<List<PagoAdminDTO>>> buscarPorReferencia(
            @Parameter(description = "Referencia de la orden", example = "ORD-001")
            @PathVariable String reference) {
        List<PagoAdminDTO> pagos = pagoService.buscarPorReferenciaAdmin(reference);
        return ResponseEntity.ok()
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Búsqueda completada", pagos));
    }

    // ─── PAGO RESPONSE ───────────────────────────────────────────────────────

    @Operation(summary = "Registrar respuesta de pago online")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Respuesta registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Pago original no encontrado")
    })
    @PostMapping("/respuesta")
    public ResponseEntity<ApiResponseDTO<PagoResponseResultDTO>> registrarRespuesta(
            @Valid @RequestBody PagoResponseDTO dto) {
        PagoResponseResultDTO result = pagoService.registrarRespuesta(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Respuesta de pago registrada exitosamente", result));
    }

    @Operation(summary = "Recibir notificación IPN de IZIPAY",
        description = "Webhook que IZIPAY invoca al final del pago (URL de notificación). " +
                      "Verifica la firma HMAC-SHA256 y actualiza el estado del pago. " +
                      "Debe responder 200 para confirmar la recepción.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "IPN recibido y procesado"),
        @ApiResponse(responseCode = "400", description = "Firma inválida o datos incorrectos")
    })
    @PostMapping("/ipn")
    public ResponseEntity<String> recibirIpn(
            @RequestParam(name = "kr-answer", required = false) String krAnswer,
            @RequestParam(name = "kr-hash", required = false) String krHash,
            @RequestParam(name = "kr-hash-key", required = false) String krHashKey) {
        boolean ok = pagoService.procesarIpn(krAnswer, krHash, krHashKey);
        if (ok) {
            return ResponseEntity.ok("OK");
        }
        return ResponseEntity.badRequest().body("FAIL");
    }

    @Operation(summary = "Listar respuestas de pago paginadas",
        description = "Parámetros: page (0-indexed), size (máx 50)")
    @ApiResponse(responseCode = "200", description = "Lista paginada de respuestas")
    @GetMapping("/respuestas")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<PagoResponse>>> listarRespuestas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        PageResponseDTO<PagoResponse> resultado = PageResponseDTO.from(pagoService.listarRespuestas(pageable));
        return ResponseEntity.ok()
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Respuestas obtenidas exitosamente", resultado));
    }

    @Operation(summary = "Obtener respuesta de pago por ID del pago original")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Respuesta encontrada"),
        @ApiResponse(responseCode = "404", description = "Respuesta no encontrada para ese pago")
    })
    @GetMapping("/{pagoId}/respuesta")
    public ResponseEntity<ApiResponseDTO<PagoResponseResultDTO>> obtenerRespuestaPorPago(
            @Parameter(description = "ID del pago original", example = "1")
            @PathVariable Long pagoId) {
        return pagoService.obtenerRespuestaPorPagoId(pagoId)
                .map(r -> ResponseEntity.ok()
                        .headers(noCacheHeaders())
                        .<ApiResponseDTO<PagoResponseResultDTO>>body(ApiResponseDTO.ok("Respuesta encontrada", r)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("No se encontró respuesta para el pago ID: " + pagoId)));
    }

    // ─── PAGO RECHAZO ────────────────────────────────────────────────────────

    @Operation(summary = "Registrar rechazo de pago online")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rechazo registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Pago original no encontrado")
    })
    @PostMapping("/rechazo")
    public ResponseEntity<ApiResponseDTO<PagoRechazo>> registrarRechazo(
            @Valid @RequestBody PagoRechazoDTO dto) {
        PagoRechazo rechazo = pagoService.registrarRechazo(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Rechazo de pago registrado exitosamente", rechazo));
    }

    @Operation(summary = "Listar rechazos de pago paginados",
        description = "Parámetros: page (0-indexed), size (máx 50)")
    @ApiResponse(responseCode = "200", description = "Lista paginada de rechazos")
    @GetMapping("/rechazos")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<PagoRechazo>>> listarRechazos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        PageResponseDTO<PagoRechazo> resultado = PageResponseDTO.from(pagoService.listarRechazos(pageable));
        return ResponseEntity.ok()
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Rechazos obtenidos exitosamente", resultado));
    }

    @Operation(summary = "Obtener rechazo por ID del pago original")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rechazo encontrado"),
        @ApiResponse(responseCode = "404", description = "Rechazo no encontrado para ese pago")
    })
    @GetMapping("/{pagoId}/rechazo")
    public ResponseEntity<ApiResponseDTO<PagoRechazo>> obtenerRechazoPorPago(
            @Parameter(description = "ID del pago original", example = "1")
            @PathVariable Long pagoId) {
        return pagoService.obtenerRechazoPorPagoId(pagoId)
                .map(r -> ResponseEntity.ok()
                        .headers(noCacheHeaders())
                        .<ApiResponseDTO<PagoRechazo>>body(ApiResponseDTO.ok("Rechazo encontrado", r)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("No se encontró rechazo para el pago ID: " + pagoId)));
    }
}
