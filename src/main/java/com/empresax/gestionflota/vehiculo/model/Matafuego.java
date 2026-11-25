package com.empresax.gestionflota.vehiculo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "Matafuego")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Matafuego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    @JsonBackReference("vehiculo-matafuegos")
    private Vehiculo vehiculo;

    @Column(length = 100)
    private String ubicacion; // Ej: "Piso Inferior", "Piso Superior", "Cabina / Piso Único"

    @Column(length = 60)
    private String numeroSerie;

    private LocalDate fechaCarga;

    @NotNull(message = "La fecha de vencimiento del matafuego es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(length = 255)
    private String observaciones;
}