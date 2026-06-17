package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de rechazo de un pago online")
public class PagoRechazoDTO {

    @NotNull(message = "El ID del pago es obligatorio")
    @Schema(description = "ID del pago original (pago_request_id)", example = "1")
    private Long pagoRequestId;

    @NotBlank(message = "El código de rechazo es obligatorio")
    @Schema(description = "Código de rechazo IZIPAY", example = "05")
    private String codigoRechazo;

    @Schema(description = "Motivo del rechazo", example = "Fondos insuficientes")
    private String motivoRechazo;

    @Schema(description = "Fecha del rechazo", example = "2026-06-17")
    private String fechaRechazo;

    @Schema(description = "Estado del rechazo", example = "RECHAZADO")
    private String estado;
}
