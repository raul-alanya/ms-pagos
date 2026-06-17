package ms_pagos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pago_response")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_respuesta", length = 255)
    private String codigoRespuesta;

    @Column(name = "codigo_transaccion", length = 255)
    private String codigoTransaccion;

    @Column(name = "estado", length = 255)
    private String estado;

    @Column(name = "fecha_respuesta", length = 255)
    private String fechaRespuesta;

    @Column(name = "mensaje", length = 255)
    private String mensaje;

    @Column(name = "pago_request_id")
    private Long pagoRequestId;
}
