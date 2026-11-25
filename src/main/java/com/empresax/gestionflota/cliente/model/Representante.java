package com.empresax.gestionflota.cliente.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Representante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Representante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String email;

    private Long telefono;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;
}
