package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de configuración para la pasarela IZIPAY")
public class ConfiguracionIzipayDTO {

    @NotBlank(message = "El código de comercio es obligatorio")
    @Size(min = 6, max = 20, message = "El código de comercio debe tener entre 6 y 20 caracteres")
    @Schema(description = "Código de comercio asignado por IZIPAY", example = "12345678")
    private String merchantCode;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    @Schema(description = "Contraseña de acceso IZIPAY", example = "testpassword_DEMOPRIVATEKEY123")
    private String passwordIzipay;

    @Schema(description = "Llave pública IZIPAY", example = "testpublickey_DEMOPUBLICKEY456")
    private String publicKey;

    @Schema(description = "Clave HMAC SHA256 para validación de firma", example = "testHmacSha256_DEMOHMAC789")
    private String hmacSha256;

    @Pattern(regexp = "^https?://.*", message = "La URL de pago debe comenzar con http:// o https://")
    @Schema(description = "URL del endpoint de pago IZIPAY", example = "https://api.micuentaweb.pe/api-payment/V4/Charge/CreatePayment")
    private String urlPago;

    @Pattern(regexp = "^https?://.*", message = "La URL de token debe comenzar con http:// o https://")
    @Schema(description = "URL del endpoint de generación de token IZIPAY", example = "https://api.micuentaweb.pe/api-payment/V4/Charge/CreateToken")
    private String urlToken;

    @Pattern(regexp = "^(PEN|USD|EUR)$", message = "La moneda debe ser PEN, USD o EUR")
    @Schema(description = "Moneda por defecto", example = "PEN")
    private String moneda;

    @Schema(description = "Indica si la configuración está activa", example = "true")
    private Boolean activo;

    @Schema(description = "ID de la empresa a la que pertenece esta configuración IZIPAY", example = "1")
    private Long empresaId;
}
