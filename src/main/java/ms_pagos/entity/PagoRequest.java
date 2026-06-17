package ms_pagos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pago_request")
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
}
