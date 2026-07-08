package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de rechazo de un pago online")
public class PagoRechazoDTO {

    @NotNull(message = "El ID del pago es obligatorio")
    @Positive(message = "El ID del pago debe ser un número positivo")
    @Schema(description = "ID del pago original (pago_request_id)", example = "1")
    private Long pagoRequestId;

    @NotBlank(message = "El código de rechazo es obligatorio")
    @Size(min = 2, max = 10, message = "El código de rechazo debe tener entre 2 y 10 caracteres")
    @Schema(description = "Código de rechazo IZIPAY", example = "05")
    private String codigoRechazo;

    @NotBlank(message = "El motivo del rechazo es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    @Schema(description = "Motivo del rechazo", example = "Fondos insuficientes")
    private String motivoRechazo;

    @Schema(description = "Fecha del rechazo (YYYY-MM-DD)", example = "2026-06-17")
    private String fechaRechazo;

    @Pattern(regexp = "^(RECHAZADO|CANCELADO)$",
             message = "El estado debe ser: RECHAZADO o CANCELADO")
    @Schema(description = "Estado del rechazo", example = "RECHAZADO")
    private String estado;
}
