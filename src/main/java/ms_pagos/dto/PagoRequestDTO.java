package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Datos de envío para procesar un pago online")
public class PagoRequestDTO {

    // INT-IZI-003: la tarjeta ya NO es obligatoria. Con el formulario hospedado
    // de IZIPAY, la captura de PAN/CVV la hace IZIPAY (nunca llega a ms-pagos).
    // Estos campos quedan solo para compatibilidad con clientes antiguos; si se
    // envían, se validan igual que antes.
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener exactamente 16 dígitos")
    @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe contener solo dígitos numéricos (0-9)")
    @Schema(description = "Número de tarjeta (opcional con formulario hospedado)", example = "4111111111111111")
    private String cardnumber;

    @Size(min = 3, max = 100, message = "El nombre del titular debe tener entre 3 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre del titular solo debe contener letras y espacios")
    @Schema(description = "Nombre del titular (opcional con formulario hospedado)", example = "Juan Perez")
    private String cardholdername;

    @Size(min = 3, max = 4, message = "El CVV debe tener 3 o 4 dígitos")
    @Pattern(regexp = "\\d{3,4}", message = "El CVV solo debe contener dígitos numéricos")
    @Schema(description = "Código de seguridad CVV (opcional con formulario hospedado)", example = "123")
    private String cvv;

    // VAL-IZI-002: formato validado aquí, vigencia validada en el service
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "La fecha de expiración debe tener formato MM/YY")
    @Schema(description = "Fecha de expiración de la tarjeta (opcional con formulario hospedado)", example = "12/26")
    private String cardexpiry;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @Schema(description = "Monto total del pago", example = "100.00")
    private Double totalamount;

    @NotBlank(message = "La referencia es obligatoria")
    @Schema(description = "Referencia única de la orden", example = "ORD-001")
    private String reference;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Schema(description = "Correo electrónico del cliente", example = "juan@gmail.com")
    private String email;

    @Size(min = 9, max = 9, message = "El teléfono debe tener exactamente 9 dígitos")
    @Pattern(regexp = "\\d{9}", message = "El teléfono solo debe contener 9 dígitos numéricos")
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

    @Pattern(regexp = "^(PEN|USD|EUR)$", message = "La moneda debe ser PEN, USD o EUR")
    @Schema(description = "Moneda de la transacción", example = "PEN")
    private String currency;

    @NotNull(message = "El ID de la caja aperturada es obligatorio")
    @Positive(message = "El ID de caja debe ser un número positivo")
    @Schema(description = "ID de la caja aperturada a la que se asocia este pago", example = "1")
    private Long cajaId;
}
