package ms_pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ms_pagos.dto.ApiResponseDTO;
import ms_pagos.dto.ConfiguracionIzipayDTO;
import ms_pagos.dto.PageResponseDTO;
import ms_pagos.dto.TokenResponseDTO;
import ms_pagos.entity.ConfiguracionIzipay;
import ms_pagos.service.ConfiguracionIzipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
@Tag(name = "Configuración IZIPAY", description = "Endpoints para gestionar la configuración de la pasarela IZIPAY")
public class ConfiguracionIzipayController {

    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private ConfiguracionIzipayService configuracionService;

    // SEC-IZI-008: Cache-Control no-store en endpoints con datos sensibles
    private HttpHeaders noCacheHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate");
        headers.set("Pragma", "no-cache");
        return headers;
    }

    @Operation(summary = "Registrar configuración IZIPAY",
        description = "Crea la configuración IZIPAY. Si activo=true (default), " +
                      "desactiva la configuración activa previa de la misma empresa (BUS-IZI-004). " +
                      "Las URLs deben ser HTTPS (SEC-IZI-006).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Configuración registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ConfiguracionIzipay>> registrarConfiguracion(
            @Valid @RequestBody ConfiguracionIzipayDTO dto) {
        ConfiguracionIzipay config = configuracionService.registrarConfiguracion(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Configuración IZIPAY registrada exitosamente", config));
    }

    @Operation(summary = "Verificar configuración IZIPAY por ID",
        description = "Verifica que la configuración exista y esté activa. " +
                      "Las credenciales (password/HMAC) NUNCA se exponen (SEC-IZI-002/004).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuración activa verificada"),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada"),
        @ApiResponse(responseCode = "409", description = "Configuración inactiva")
    })
    @PostMapping("/{id}/token")
    public ResponseEntity<ApiResponseDTO<TokenResponseDTO>> generarToken(
            @Parameter(description = "ID de la configuración IZIPAY", example = "1")
            @PathVariable Long id) {
        TokenResponseDTO token = configuracionService.generarToken(id);
        return ResponseEntity.ok()
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Configuración activa verificada exitosamente", token));
    }

    @Operation(summary = "Verificar configuración IZIPAY activa",
        description = "Verifica la configuración activa de la empresa. Las credenciales nunca se exponen.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuración activa verificada"),
        @ApiResponse(responseCode = "404", description = "No existe configuración activa")
    })
    @PostMapping("/token/activo")
    public ResponseEntity<ApiResponseDTO<TokenResponseDTO>> generarTokenActivo() {
        TokenResponseDTO token = configuracionService.generarTokenActivo();
        return ResponseEntity.ok()
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Configuración activa verificada exitosamente", token));
    }

    @Operation(summary = "Listar configuraciones IZIPAY paginadas",
        description = "password, publicKey y hmacSha256 nunca aparecen en la respuesta (SEC-IZI-002). " +
                      "Parámetros: page (0-indexed), size (máx 50)")
    @ApiResponse(responseCode = "200", description = "Lista paginada de configuraciones")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ConfiguracionIzipay>>> listarConfiguraciones(
            @Parameter(description = "Número de página", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (máximo 50)", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        PageResponseDTO<ConfiguracionIzipay> resultado =
                PageResponseDTO.from(configuracionService.listarConfiguraciones(pageable));
        return ResponseEntity.ok()
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Configuraciones obtenidas exitosamente", resultado));
    }

    @Operation(summary = "Obtener configuración por ID",
        description = "password, publicKey y hmacSha256 nunca aparecen en la respuesta (SEC-IZI-002)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuración encontrada"),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ConfiguracionIzipay>> obtenerConfiguracion(
            @Parameter(description = "ID de la configuración", example = "1") @PathVariable Long id) {
        return configuracionService.obtenerPorId(id)
                .map(c -> ResponseEntity.ok()
                        .headers(noCacheHeaders())
                        .<ApiResponseDTO<ConfiguracionIzipay>>body(ApiResponseDTO.ok("Configuración encontrada", c)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Configuración no encontrada con ID: " + id)));
    }

    @Operation(summary = "Actualizar configuración IZIPAY",
        description = "Si activo=true, desactiva otras configs activas de la misma empresa (BUS-IZI-004). " +
                      "Las URLs deben ser HTTPS (SEC-IZI-006).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuración actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ConfiguracionIzipay>> actualizarConfiguracion(
            @Parameter(description = "ID de la configuración", example = "1") @PathVariable Long id,
            @Valid @RequestBody ConfiguracionIzipayDTO dto) {
        ConfiguracionIzipay config = configuracionService.actualizarConfiguracion(id, dto);
        return ResponseEntity.ok()
                .headers(noCacheHeaders())
                .body(ApiResponseDTO.ok("Configuración actualizada exitosamente", config));
    }
}
