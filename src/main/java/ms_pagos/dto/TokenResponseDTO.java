package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de generación de token IZIPAY")
public class TokenResponseDTO {

    @Schema(description = "Token de comunicación generado", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Código de comercio usado", example = "12345678")
    private String merchantCode;

    @Schema(description = "URL de pago IZIPAY", example = "https://api.micuentaweb.pe/api-payment/V4/Charge/CreatePayment")
    private String urlPago;

    @Schema(description = "Mensaje del resultado", example = "Token generado exitosamente")
    private String mensaje;

    @Schema(description = "Estado de la operación", example = "SUCCESS")
    private String estado;
}
