package com.empresax.gestionflota.viaje.model;

import com.empresax.gestionflota.chofer.model.Chofer;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "AsignacionChofer")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AsignacionChofer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String rol;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_viaje_id", nullable = false)
    @JsonBackReference
    private UnidadViaje unidadViaje;

    // Relación con Chofer (Sprint 1)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chofer_id")
    private Chofer chofer;

    // Datos en caso de chofer externo/tercero
    private String nombreTercero;
    private String dniTercero;
    private String telefonoTercero;

    public Chofer conocerChofer() {
        return this.chofer;
    }
}