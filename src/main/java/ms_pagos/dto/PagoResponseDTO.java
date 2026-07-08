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
@Schema(description = "Datos de respuesta de pago online recibidos desde IZIPAY")
public class PagoResponseDTO {

    @NotNull(message = "El ID del pago es obligatorio")
    @Positive(message = "El ID del pago debe ser un número positivo")
    @Schema(description = "ID del pago original (pago_request_id)", example = "1")
    private Long pagoRequestId;

    @NotBlank(message = "El código de respuesta es obligatorio")
    @Size(min = 2, max = 10, message = "El código de respuesta debe tener entre 2 y 10 caracteres")
    @Schema(description = "Código de respuesta IZIPAY", example = "00")
    private String codigoRespuesta;

    @Schema(description = "Código único de la transacción IZIPAY", example = "TXN-20260617-001")
    private String codigoTransaccion;

    @Pattern(regexp = "^(APROBADO|RECHAZADO|ERROR|PENDIENTE)$",
             message = "El estado debe ser: APROBADO, RECHAZADO, ERROR o PENDIENTE")
    @Schema(description = "Estado de la transacción", example = "APROBADO")
    private String estado;

    @Schema(description = "Fecha de la respuesta (YYYY-MM-DD)", example = "2026-06-17")
    private String fechaRespuesta;

    @Schema(description = "Mensaje descriptivo de la respuesta", example = "Pago aprobado exitosamente")
    private String mensaje;
}
