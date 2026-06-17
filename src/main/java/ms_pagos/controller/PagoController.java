package ms_pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ms_pagos.dto.ApiResponseDTO;
import ms_pagos.dto.PagoRechazoDTO;
import ms_pagos.dto.PagoRequestDTO;
import ms_pagos.dto.PagoResponseDTO;
import ms_pagos.entity.PagoRechazo;
import ms_pagos.entity.PagoRequest;
import ms_pagos.entity.PagoResponse;
import ms_pagos.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos Online", description = "Endpoints para gestionar pagos online, respuestas y rechazos IZIPAY")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    // ─── PAGO REQUEST ────────────────────────────────────────────────────────

    @Operation(
        summary = "Registrar datos de envío de pago online",
        description = "Registra los datos del pago enviado a IZIPAY: información de tarjeta, " +
                      "monto, referencia y datos del cliente"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del pago inválidos")
    })
    @PostMapping("/procesar")
    public ResponseEntity<ApiResponseDTO<PagoRequest>> registrarPago(
            @Valid @RequestBody PagoRequestDTO dto) {
        try {
            PagoRequest pago = pagoService.registrarPago(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.ok("Datos de pago registrados exitosamente", pago));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Error al registrar pago: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Listar todos los pagos registrados",
        description = "Retorna la lista de todos los pagos online enviados a IZIPAY"
    )
    @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida exitosamente")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PagoRequest>>> listarPagos() {
        List<PagoRequest> pagos = pagoService.listarPagos();
        return ResponseEntity.ok(ApiResponseDTO.ok("Pagos obtenidos exitosamente", pagos));
    }

    @Operation(
        summary = "Obtener pago por ID",
        description = "Retorna los datos de un pago específico por su ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pago encontrado"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PagoRequest>> obtenerPago(
            @Parameter(description = "ID del pago", example = "1")
            @PathVariable Long id) {
        return pagoService.obtenerPagoPorId(id)
                .map(pago -> ResponseEntity.ok(
                        ApiResponseDTO.ok("Pago encontrado", pago)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Pago no encontrado con ID: " + id)));
    }

    @Operation(
        summary = "Buscar pagos por referencia",
        description = "Retorna los pagos que coincidan con la referencia de orden indicada"
    )
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    @GetMapping("/referencia/{reference}")
    public ResponseEntity<ApiResponseDTO<List<PagoRequest>>> buscarPorReferencia(
            @Parameter(description = "Referencia de la orden", example = "ORD-001")
            @PathVariable String reference) {
        List<PagoRequest> pagos = pagoService.buscarPorReferencia(reference);
        return ResponseEntity.ok(ApiResponseDTO.ok("Búsqueda completada", pagos));
    }

    // ─── PAGO RESPONSE ───────────────────────────────────────────────────────

    @Operation(
        summary = "Registrar respuesta de pago online (código de operación)",
        description = "Registra los datos de respuesta recibidos desde IZIPAY: código de operación, " +
                      "código de transacción y estado del pago procesado"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Respuesta de pago registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Pago original no encontrado")
    })
    @PostMapping("/respuesta")
    public ResponseEntity<ApiResponseDTO<PagoResponse>> registrarRespuesta(
            @Valid @RequestBody PagoResponseDTO dto) {
        try {
            PagoResponse response = pagoService.registrarRespuesta(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.ok("Respuesta de pago registrada exitosamente", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Error al registrar respuesta: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Listar todas las respuestas de pago",
        description = "Retorna la lista de todas las respuestas recibidas desde IZIPAY"
    )
    @ApiResponse(responseCode = "200", description = "Lista de respuestas obtenida exitosamente")
    @GetMapping("/respuestas")
    public ResponseEntity<ApiResponseDTO<List<PagoResponse>>> listarRespuestas() {
        List<PagoResponse> respuestas = pagoService.listarRespuestas();
        return ResponseEntity.ok(ApiResponseDTO.ok("Respuestas obtenidas exitosamente", respuestas));
    }

    @Operation(
        summary = "Obtener respuesta de pago por ID del pago original",
        description = "Retorna la respuesta IZIPAY asociada al pago con el ID especificado"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Respuesta encontrada"),
        @ApiResponse(responseCode = "404", description = "Respuesta no encontrada para ese pago")
    })
    @GetMapping("/{pagoId}/respuesta")
    public ResponseEntity<ApiResponseDTO<PagoResponse>> obtenerRespuestaPorPago(
            @Parameter(description = "ID del pago original", example = "1")
            @PathVariable Long pagoId) {
        return pagoService.obtenerRespuestaPorPagoId(pagoId)
                .map(resp -> ResponseEntity.ok(
                        ApiResponseDTO.ok("Respuesta encontrada", resp)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("No se encontró respuesta para el pago ID: " + pagoId)));
    }

    // ─── PAGO RECHAZO ────────────────────────────────────────────────────────

    @Operation(
        summary = "Registrar rechazo de pago online",
        description = "Registra el rechazo de un pago online con el código y motivo devuelto por IZIPAY"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rechazo registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Pago original no encontrado")
    })
    @PostMapping("/rechazo")
    public ResponseEntity<ApiResponseDTO<PagoRechazo>> registrarRechazo(
            @Valid @RequestBody PagoRechazoDTO dto) {
        try {
            PagoRechazo rechazo = pagoService.registrarRechazo(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.ok("Rechazo de pago registrado exitosamente", rechazo));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Error al registrar rechazo: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Listar todos los rechazos de pago",
        description = "Retorna la lista de todos los pagos rechazados registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de rechazos obtenida exitosamente")
    @GetMapping("/rechazos")
    public ResponseEntity<ApiResponseDTO<List<PagoRechazo>>> listarRechazos() {
        List<PagoRechazo> rechazos = pagoService.listarRechazos();
        return ResponseEntity.ok(ApiResponseDTO.ok("Rechazos obtenidos exitosamente", rechazos));
    }

    @Operation(
        summary = "Obtener rechazo por ID del pago original",
        description = "Retorna el registro de rechazo asociado al pago con el ID especificado"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rechazo encontrado"),
        @ApiResponse(responseCode = "404", description = "Rechazo no encontrado para ese pago")
    })
    @GetMapping("/{pagoId}/rechazo")
    public ResponseEntity<ApiResponseDTO<PagoRechazo>> obtenerRechazoPorPago(
            @Parameter(description = "ID del pago original", example = "1")
            @PathVariable Long pagoId) {
        return pagoService.obtenerRechazoPorPagoId(pagoId)
                .map(rechazo -> ResponseEntity.ok(
                        ApiResponseDTO.ok("Rechazo encontrado", rechazo)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("No se encontró rechazo para el pago ID: " + pagoId)));
    }
}
