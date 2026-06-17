package ms_pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ms_pagos.dto.ApiResponseDTO;
import ms_pagos.dto.ConfiguracionIzipayDTO;
import ms_pagos.dto.TokenResponseDTO;
import ms_pagos.entity.ConfiguracionIzipay;
import ms_pagos.service.ConfiguracionIzipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuracion")
@Tag(name = "Configuración IZIPAY", description = "Endpoints para gestionar la configuración de la pasarela de pagos IZIPAY")
public class ConfiguracionIzipayController {

    @Autowired
    private ConfiguracionIzipayService configuracionService;

    @Operation(
        summary = "Registrar configuración IZIPAY",
        description = "Registra los datos de configuración necesarios para conectarse con la pasarela IZIPAY " +
                      "(merchant code, password, llaves de seguridad y URLs de endpoints)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Configuración registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de configuración inválidos")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ConfiguracionIzipay>> registrarConfiguracion(
            @Valid @RequestBody ConfiguracionIzipayDTO dto) {
        try {
            ConfiguracionIzipay config = configuracionService.registrarConfiguracion(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.ok("Configuración IZIPAY registrada exitosamente", config));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Error al registrar configuración: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Generar token de comunicación IZIPAY",
        description = "Genera el token de comunicación (Basic Auth en Base64) usando las credenciales " +
                      "de la configuración especificada por ID. Este token se usa para autenticar las " +
                      "solicitudes hacia los endpoints de IZIPAY."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token generado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada")
    })
    @PostMapping("/{id}/token")
    public ResponseEntity<ApiResponseDTO<TokenResponseDTO>> generarToken(
            @Parameter(description = "ID de la configuración IZIPAY", example = "1")
            @PathVariable Long id) {
        try {
            TokenResponseDTO token = configuracionService.generarToken(id);
            return ResponseEntity.ok(ApiResponseDTO.ok("Token generado exitosamente", token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @Operation(
        summary = "Generar token con configuración activa",
        description = "Genera el token de comunicación usando la configuración IZIPAY que esté activa"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token generado exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existe configuración activa")
    })
    @PostMapping("/token/activo")
    public ResponseEntity<ApiResponseDTO<TokenResponseDTO>> generarTokenActivo() {
        try {
            TokenResponseDTO token = configuracionService.generarTokenActivo();
            return ResponseEntity.ok(ApiResponseDTO.ok("Token generado exitosamente", token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @Operation(
        summary = "Listar todas las configuraciones IZIPAY",
        description = "Retorna la lista de todas las configuraciones registradas en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de configuraciones obtenida exitosamente")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ConfiguracionIzipay>>> listarConfiguraciones() {
        List<ConfiguracionIzipay> configs = configuracionService.listarConfiguraciones();
        return ResponseEntity.ok(ApiResponseDTO.ok("Configuraciones obtenidas exitosamente", configs));
    }

    @Operation(
        summary = "Obtener configuración por ID",
        description = "Retorna los datos de una configuración IZIPAY específica por su ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuración encontrada"),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ConfiguracionIzipay>> obtenerConfiguracion(
            @Parameter(description = "ID de la configuración", example = "1")
            @PathVariable Long id) {
        return configuracionService.obtenerPorId(id)
                .map(config -> ResponseEntity.ok(
                        ApiResponseDTO.ok("Configuración encontrada", config)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Configuración no encontrada con ID: " + id)));
    }

    @Operation(
        summary = "Actualizar configuración IZIPAY",
        description = "Actualiza los datos de una configuración IZIPAY existente"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuración actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ConfiguracionIzipay>> actualizarConfiguracion(
            @Parameter(description = "ID de la configuración", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ConfiguracionIzipayDTO dto) {
        try {
            ConfiguracionIzipay config = configuracionService.actualizarConfiguracion(id, dto);
            return ResponseEntity.ok(ApiResponseDTO.ok("Configuración actualizada exitosamente", config));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }
}
