package ms_pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ms_pagos.dto.ApiResponseDTO;
import ms_pagos.dto.EmpresaDTO;
import ms_pagos.dto.PageResponseDTO;
import ms_pagos.entity.Empresa;
import ms_pagos.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Endpoints para registrar y gestionar empresas que usan la pasarela IZIPAY")
public class EmpresaController {

    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private EmpresaService empresaService;

    @Operation(summary = "Registrar nueva empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empresa registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "RUC o email duplicado")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Empresa>> registrarEmpresa(
            @Valid @RequestBody EmpresaDTO dto) {
        Empresa empresa = empresaService.registrarEmpresa(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Empresa registrada exitosamente", empresa));
    }

    @Operation(summary = "Listar todas las empresas paginadas",
        description = "Parámetros: page (0-indexed), size (máx 50), sort, direction")
    @ApiResponse(responseCode = "200", description = "Lista paginada de empresas")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<Empresa>>> listarEmpresas(
            @Parameter(description = "Número de página", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (máximo 50)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de orden", example = "id")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Dirección: asc o desc", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {

        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(dir, sort));
        PageResponseDTO<Empresa> resultado = PageResponseDTO.from(empresaService.listarEmpresas(pageable));
        return ResponseEntity.ok(ApiResponseDTO.ok("Empresas obtenidas exitosamente", resultado));
    }

    @Operation(summary = "Listar empresas activas paginadas",
        description = "Solo retorna empresas con activo=true. Parámetros: page, size (máx 50)")
    @ApiResponse(responseCode = "200", description = "Lista paginada de empresas activas")
    @GetMapping("/activas")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<Empresa>>> listarEmpresasActivas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.ASC, "nombre"));
        PageResponseDTO<Empresa> resultado = PageResponseDTO.from(
                empresaService.listarEmpresasActivas(pageable));
        return ResponseEntity.ok(ApiResponseDTO.ok("Empresas activas obtenidas exitosamente", resultado));
    }

    @Operation(summary = "Obtener empresa por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Empresa>> obtenerEmpresa(
            @Parameter(description = "ID de la empresa", example = "1") @PathVariable Long id) {
        return empresaService.obtenerPorId(id)
                .map(e -> ResponseEntity.ok(ApiResponseDTO.ok("Empresa encontrada", e)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Empresa no encontrada con ID: " + id)));
    }

    @Operation(summary = "Buscar empresa por RUC")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<ApiResponseDTO<Empresa>> obtenerPorRuc(
            @Parameter(description = "RUC de la empresa", example = "20123456789")
            @PathVariable String ruc) {
        return empresaService.obtenerPorRuc(ruc)
                .map(e -> ResponseEntity.ok(ApiResponseDTO.ok("Empresa encontrada", e)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Empresa no encontrada con RUC: " + ruc)));
    }

    @Operation(summary = "Actualizar empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada"),
        @ApiResponse(responseCode = "409", description = "RUC pertenece a otra empresa")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Empresa>> actualizarEmpresa(
            @Parameter(description = "ID de la empresa", example = "1") @PathVariable Long id,
            @Valid @RequestBody EmpresaDTO dto) {
        Empresa empresa = empresaService.actualizarEmpresa(id, dto);
        return ResponseEntity.ok(ApiResponseDTO.ok("Empresa actualizada exitosamente", empresa));
    }

    @Operation(summary = "Desactivar empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa desactivada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponseDTO<Empresa>> desactivarEmpresa(
            @Parameter(description = "ID de la empresa", example = "1") @PathVariable Long id) {
        Empresa empresa = empresaService.desactivarEmpresa(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Empresa desactivada exitosamente", empresa));
    }

    @Operation(summary = "Eliminar empresa",
        description = "Elimina la empresa (borrado lógico: queda inactiva para no romper " +
                      "las llaves foráneas de sus configuraciones IZIPAY).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Empresa>> eliminarEmpresa(
            @Parameter(description = "ID de la empresa", example = "1") @PathVariable Long id) {
        Empresa empresa = empresaService.eliminarEmpresa(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Empresa eliminada exitosamente", empresa));
    }
}
