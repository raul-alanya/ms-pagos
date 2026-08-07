package ms_pagos.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion_izipay")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionIzipay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_code", nullable = false, length = 100)
    private String merchantCode;

    // SEC-IZI-002: nunca exponer la contraseña en responses
    @Column(name = "password_izipay", nullable = false, length = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordIzipay;

    // SEC-IZI-002: nunca exponer la llave pública en responses
    @Column(name = "public_key", length = 500)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String publicKey;

    // SEC-IZI-002: nunca exponer el HMAC en responses
    @Column(name = "hmac_sha256", length = 500)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String hmacSha256;

    @Column(name = "url_pago", length = 500)
    private String urlPago;

    @Column(name = "url_token", length = 500)
    private String urlToken;

    @Column(name = "moneda", length = 10)
    private String moneda;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_registro", length = 255)
    private String fechaRegistro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id", referencedColumnName = "id")
    private Empresa empresa;
}
