package ms_pagos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pago_rechazo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRechazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_rechazo", length = 255)
    private String codigoRechazo;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @Column(name = "fecha_rechazo", length = 255)
    private String fechaRechazo;

    @Column(name = "estado", length = 255)
    private String estado;

    @Column(name = "pago_request_id")
    private Long pagoRequestId;
}
