package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import ms_pagos.entity.PagoRequest;

import java.time.LocalDateTime;

/**
 * DTO para el frontend administrador.
 * Consulta de pagos: aprobados, rechazados y pendientes, con los campos
 * necesarios para listar, filtrar y sumar montos.
 */
@Data
@NoArgsConstructor
@Schema(description = "Datos de pago para el panel administrador")
public class PagoAdminDTO {

    @Schema(description = "ID del pago", example = "38")
    private Long id;

    @Schema(description = "Monto total del pago", example = "49.99")
    private Double totalamount;

    @Schema(description = "Referencia única de la orden", example = "ORD-1786239304703")
    private String reference;

    @Schema(description = "Correo electrónico del cliente", example = "cliente@email.com")
    private String email;

    @Schema(description = "Moneda de la transacción", example = "PEN")
    private String currency;

    @Schema(description = "Estado del pago", example = "APROBADO")
    private String estado;

    @Schema(description = "Tipo de transacción", example = "Sale")
    private String transactiontype;

    @Schema(description = "Fecha de creación del pago", example = "2026-08-10T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "ID de la caja aperturada", example = "1")
    private Long cajaId;

    public static PagoAdminDTO from(PagoRequest pago) {
        PagoAdminDTO dto = new PagoAdminDTO();
        dto.setId(pago.getId());
        dto.setTotalamount(pago.getTotalamount());
        dto.setReference(pago.getReference());
        dto.setEmail(pago.getEmail());
        dto.setCurrency(pago.getCurrency());
        dto.setEstado(pago.getEstado());
        dto.setTransactiontype(pago.getTransactiontype());
        dto.setCreatedAt(pago.getCreatedAt());
        dto.setCajaId(pago.getCajaId());
        return dto;
    }
}
