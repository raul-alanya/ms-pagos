package ms_pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ms_pagos.dto.ApiResponseDTO;
import ms_pagos.dto.EmpresaDTO;
import ms_pagos.entity.Empresa;
import ms_pagos.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Endpoints para registrar y gestionar empresas que usan la pasarela IZIPAY")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    @Operation(
        summary = "Registrar nueva empresa",
        description = "Registra una empresa en el sistema. Cada empresa tendrá su propia " +
                      "configuración IZIPAY con sus credenciales (merchant code, password, llaves). " +
                      "Así los datos de IZIPAY no son personales sino de la empresa."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empresa registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o RUC/email duplicado")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Empresa>> registrarEmpresa(
            @Valid @RequestBody EmpresaDTO dto) {
        try {
            Empresa empresa = empresaService.registrarEmpresa(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.ok("Empresa registrada exitosamente", empresa));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @Operation(
        summary = "Listar todas las empresas",
        description = "Retorna la lista de todas las empresas registradas en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<Empresa>>> listarEmpresas() {
        List<Empresa> empresas = empresaService.listarEmpresas();
        return ResponseEntity.ok(ApiResponseDTO.ok("Empresas obtenidas exitosamente", empresas));
    }

    @Operation(
        summary = "Listar empresas activas",
        description = "Retorna solo las empresas que están activas en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    @GetMapping("/activas")
    public ResponseEntity<ApiResponseDTO<List<Empresa>>> listarEmpresasActivas() {
        List<Empresa> empresas = empresaService.listarEmpresasActivas();
        return ResponseEntity.ok(ApiResponseDTO.ok("Empresas activas obtenidas exitosamente", empresas));
    }

    @Operation(
        summary = "Obtener empresa por ID",
        description = "Retorna los datos de una empresa específica por su ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Empresa>> obtenerEmpresa(
            @Parameter(description = "ID de la empresa", example = "1")
            @PathVariable Long id) {
        return empresaService.obtenerPorId(id)
                .map(e -> ResponseEntity.ok(ApiResponseDTO.ok("Empresa encontrada", e)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Empresa no encontrada con ID: " + id)));
    }

    @Operation(
        summary = "Buscar empresa por RUC",
        description = "Retorna los datos de una empresa buscando por su RUC"
    )
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

    @Operation(
        summary = "Actualizar empresa",
        description = "Actualiza los datos de una empresa existente"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Empresa>> actualizarEmpresa(
            @Parameter(description = "ID de la empresa", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EmpresaDTO dto) {
        try {
            Empresa empresa = empresaService.actualizarEmpresa(id, dto);
            return ResponseEntity.ok(ApiResponseDTO.ok("Empresa actualizada exitosamente", empresa));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @Operation(
        summary = "Desactivar empresa",
        description = "Desactiva una empresa del sistema (no la elimina)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa desactivada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponseDTO<Empresa>> desactivarEmpresa(
            @Parameter(description = "ID de la empresa", example = "1")
            @PathVariable Long id) {
        try {
            Empresa empresa = empresaService.desactivarEmpresa(id);
            return ResponseEntity.ok(ApiResponseDTO.ok("Empresa desactivada exitosamente", empresa));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }
}
