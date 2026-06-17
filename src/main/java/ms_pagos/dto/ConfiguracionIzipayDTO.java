package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de configuración para la pasarela IZIPAY")
public class ConfiguracionIzipayDTO {

    @NotBlank(message = "El código de comercio es obligatorio")
    @Schema(description = "Código de comercio asignado por IZIPAY", example = "12345678")
    private String merchantCode;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña de acceso IZIPAY", example = "testpassword_DEMOPRIVATEKEY123")
    private String passwordIzipay;

    @Schema(description = "Llave pública IZIPAY", example = "testpublickey_DEMOPUBLICKEY456")
    private String publicKey;

    @Schema(description = "Clave HMAC SHA256 para validación de firma", example = "testHmacSha256_DEMOHMAC789")
    private String hmacSha256;

    @Schema(description = "URL del endpoint de pago IZIPAY", example = "https://api.micuentaweb.pe/api-payment/V4/Charge/CreatePayment")
    private String urlPago;

    @Schema(description = "URL del endpoint de generación de token IZIPAY", example = "https://api.micuentaweb.pe/api-payment/V4/Charge/CreateToken")
    private String urlToken;

    @Schema(description = "Moneda por defecto", example = "PEN")
    private String moneda;

    @Schema(description = "Indica si la configuración está activa", example = "true")
    private Boolean activo;
}
