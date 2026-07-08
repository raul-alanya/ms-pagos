package ms_pagos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "telefono", length = 9)
    private String telefono;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "representante_legal", length = 150)
    private String representanteLegal;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_registro", length = 255)
    private String fechaRegistro;
}
