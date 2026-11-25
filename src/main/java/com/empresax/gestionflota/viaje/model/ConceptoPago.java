package com.empresax.gestionflota.viaje.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ConceptoPago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // "Seña", "Saldo"

    public String conocerNombre() {
        return this.nombre;
    }
}