package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de envío para procesar un pago online")
public class PagoRequestDTO {

    @NotBlank(message = "El número de tarjeta es obligatorio")
    @Schema(description = "Número de tarjeta de crédito/débito", example = "4111111111111111")
    private String cardnumber;

    @NotBlank(message = "El nombre del titular es obligatorio")
    @Schema(description = "Nombre del titular de la tarjeta", example = "Juan Perez")
    private String cardholdername;

    @NotBlank(message = "El CVV es obligatorio")
    @Schema(description = "Código de seguridad CVV", example = "123")
    private String cvv;

    @NotBlank(message = "La fecha de expiración es obligatoria")
    @Schema(description = "Fecha de expiración de la tarjeta (MM/YY)", example = "12/26")
    private String cardexpiry;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @Schema(description = "Monto total del pago", example = "100.00")
    private Double totalamount;

    @NotBlank(message = "La referencia es obligatoria")
    @Schema(description = "Referencia única de la orden", example = "ORD-001")
    private String reference;

    @NotBlank(message = "El correo es obligatorio")
    @Schema(description = "Correo electrónico del cliente", example = "juan@gmail.com")
    private String email;

    @Schema(description = "Teléfono del cliente", example = "987654321")
    private String phone;

    @Schema(description = "IP del cliente", example = "192.168.1.1")
    private String clientip;

    @Schema(description = "País del cliente (código ISO)", example = "PE")
    private String clientcountry;

    @Schema(description = "URL del sitio del cliente", example = "https://mitienda.com")
    private String clienturl;

    @Schema(description = "Tipo de transacción", example = "Sale")
    private String transactiontype;

    @Schema(description = "Moneda de la transacción", example = "PEN")
    private String currency;
}
