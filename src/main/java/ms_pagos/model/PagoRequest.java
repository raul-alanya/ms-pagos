package ms_pagos.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pago_request")
public class PagoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardnumber;
    private String cardholdername;
    private String cvv;
    private String cardexpiry;
    private Double totalamount;
    private String reference;
    private String email;
    private String phone;
    private String clientip;
    private String clientcountry;
    private String clienturl;
    private String transactiontype;
    private String currency;
}