package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de registro de una empresa en el sistema de pagos")
public class EmpresaDTO {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    @Schema(description = "Nombre de la empresa", example = "TiendaMas SAC")
    private String nombre;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11, message = "El RUC debe tener exactamente 11 dígitos")
    @Pattern(regexp = "\\d{11}", message = "El RUC solo debe contener dígitos numéricos")
    @Schema(description = "RUC de la empresa (11 dígitos)", example = "20123456789")
    private String ruc;

    @Size(max = 255, message = "La dirección no debe superar 255 caracteres")
    @Schema(description = "Dirección de la empresa", example = "Av. Javier Prado 1234, Lima")
    private String direccion;

    @Size(min = 9, max = 9, message = "El teléfono debe tener exactamente 9 dígitos")
    @Pattern(regexp = "\\d{9}", message = "El teléfono solo debe contener dígitos numéricos")
    @Schema(description = "Teléfono de la empresa", example = "987654321")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Schema(description = "Correo electrónico de la empresa", example = "contacto@tiendamas.com")
    private String email;

    @Size(max = 150, message = "El nombre del representante no debe superar 150 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$",
             message = "El nombre del representante solo debe contener letras y espacios")
    @Schema(description = "Nombre del representante legal", example = "Carlos Mendoza")
    private String representanteLegal;

    @Schema(description = "Indica si la empresa está activa", example = "true")
    private Boolean activo;
}
