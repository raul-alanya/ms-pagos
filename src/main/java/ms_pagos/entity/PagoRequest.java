package ms_pagos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
// BUS-IZI-002: referencia única por transacción para evitar cargos duplicados
@Table(name = "pago_request", uniqueConstraints = {
    @UniqueConstraint(name = "uk_pago_request_reference", columnNames = {"reference"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cardnumber", length = 255)
    private String cardnumber;

    @Column(name = "cardholdername", length = 255)
    private String cardholdername;

    // SEC-IZI-003: CVV almacenado internamente pero nunca expuesto en responses (via PagoSafeDTO)
    @Column(name = "cvv", length = 255)
    private String cvv;

    @Column(name = "cardexpiry", length = 255)
    private String cardexpiry;

    @Column(name = "totalamount")
    private Double totalamount;

    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "clientip", length = 255)
    private String clientip;

    @Column(name = "clientcountry", length = 255)
    private String clientcountry;

    @Column(name = "clienturl", length = 255)
    private String clienturl;

    @Column(name = "transactiontype", length = 255)
    private String transactiontype;

    @Column(name = "currency", length = 255)
    private String currency;

    @Column(name = "caja_id")
    private Long cajaId;

    @Column(name = "estado", length = 50)
    private String estado;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
