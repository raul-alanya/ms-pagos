package ms_pagos.entity;

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

    @Column(name = "password_izipay", nullable = false, length = 255)
    private String passwordIzipay;

    @Column(name = "public_key", length = 500)
    private String publicKey;

    @Column(name = "hmac_sha256", length = 500)
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
}
