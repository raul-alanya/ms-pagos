package ms_pagos.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pago_response")
public class PagoResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pago_request_id")
    private PagoRequest pagoRequest;

    private String codigoRespuesta;
    private String estado;
    private String codigoTransaccion;
    private String mensaje;
    private String fechaRespuesta;
}