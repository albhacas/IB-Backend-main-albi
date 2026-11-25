package com.empresax.gestionflota.viaje.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MedioPago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedioPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // "Efectivo", "Transferencia", "Tarjeta de Débito", "Tarjeta de Crédito", "Cheque"

    public String conocerNombre() {
        return this.nombre;
    }
}