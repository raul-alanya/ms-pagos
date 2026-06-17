package ms_pagos.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pago_rechazo")
public class PagoRechazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pago_request_id")
    private PagoRequest pagoRequest;

    private String codigoRechazo;
    private String motivoRechazo;
    private String fechaRechazo;
    private String estado;
}