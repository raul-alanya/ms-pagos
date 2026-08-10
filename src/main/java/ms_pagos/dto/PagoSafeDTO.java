package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import ms_pagos.entity.PagoRequest;

/**
 * SEC-IZI-003: DTO de salida seguro para PagoRequest.
 * - cardnumber: solo últimos 4 dígitos, resto enmascarado (****-****-****-1234)
 * - cvv: nunca expuesto
 * - cardexpiry: nunca expuesto
 */
@Data
@NoArgsConstructor
@Schema(description = "Datos de pago con información sensible enmascarada")
public class PagoSafeDTO {

    @Schema(description = "ID del pago", example = "1")
    private Long id;

    @Schema(description = "Número de tarjeta enmascarado", example = "****-****-****-1111")
    private String cardnumber;

    @Schema(description = "Nombre del titular de la tarjeta", example = "Juan Perez")
    private String cardholdername;

    @Schema(description = "Monto total del pago", example = "100.00")
    private Double totalamount;

    @Schema(description = "Referencia única de la orden", example = "ORD-001")
    private String reference;

    @Schema(description = "Correo electrónico del cliente", example = "juan@gmail.com")
    private String email;

    @Schema(description = "Moneda de la transacción", example = "PEN")
    private String currency;

    @Schema(description = "ID de la caja aperturada", example = "1")
    private Long cajaId;

    @Schema(description = "Estado del pago", example = "PENDIENTE")
    private String estado;

    @Schema(description = "Tipo de transacción", example = "Sale")
    private String transactiontype;

    @Schema(description = "Token del formulario de pago hospedado de IZIPAY", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String formToken;

    @Schema(description = "Clave pública IZIPAY para desplegar el formulario de pago", example = "69332831:testpublickey_...")
    private String publicKey;

    @Schema(description = "URL del script JavaScript del formulario de pago de IZIPAY", example = "https://static.micuentaweb.pe/static/js/krypton-client/V4.0/stable/kr-payment-form.min.js")
    private String urlJs;

    /**
     * Construye un PagoSafeDTO enmascarando el PAN y omitiendo CVV y cardexpiry.
     */
    public static PagoSafeDTO from(PagoRequest pago) {
        PagoSafeDTO dto = new PagoSafeDTO();
        dto.setId(pago.getId());
        dto.setCardholdername(pago.getCardholdername());
        dto.setTotalamount(pago.getTotalamount());
        dto.setReference(pago.getReference());
        dto.setEmail(pago.getEmail());
        dto.setCurrency(pago.getCurrency());
        dto.setCajaId(pago.getCajaId());
        dto.setEstado(pago.getEstado());
        dto.setTransactiontype(pago.getTransactiontype());

        String pan = pago.getCardnumber();
        if (pan != null && pan.length() >= 4) {
            dto.setCardnumber("****-****-****-" + pan.substring(pan.length() - 4));
        } else {
            dto.setCardnumber("****-****-****-????");
        }

        return dto;
    }
}
