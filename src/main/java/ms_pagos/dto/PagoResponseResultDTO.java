package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de la respuesta de un pago online")
public class PagoResponseResultDTO {

    @Schema(description = "ID de la respuesta", example = "1")
    private Long id;

    @Schema(description = "Código único de la transacción", example = "TXN-20260617-0001")
    private String codigoTransaccion;

    @Schema(description = "Referencia de la orden asociada", example = "ORD-001")
    private String referencia;

    @Schema(description = "Código de respuesta IZIPAY", example = "00")
    private String codigoRespuesta;

    @Schema(description = "Estado del pago", example = "APROBADO")
    private String estado;

    @Schema(description = "Fecha de la respuesta", example = "2026-06-17")
    private String fechaRespuesta;

    @Schema(description = "Mensaje descriptivo", example = "Pago aprobado exitosamente")
    private String mensaje;
}
