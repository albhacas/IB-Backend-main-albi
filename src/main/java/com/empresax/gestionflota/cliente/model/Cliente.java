package com.empresax.gestionflota.cliente.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 20)
    private String cuit;

    @Column(length = 255)
    private String direccion;

    private Long telefono;

    @Column(length = 100)
    private String email;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "representante_id", nullable = false, unique = true)
    private Representante representante;
}
