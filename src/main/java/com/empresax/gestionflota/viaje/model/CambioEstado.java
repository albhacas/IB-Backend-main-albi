package com.empresax.gestionflota.viaje.model;

import com.empresax.gestionflota.viaje.model.estado.EstadoViaje;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CambioEstado")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CambioEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    @JsonBackReference
    private Viaje viaje;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoViaje estado;

    @Column(nullable = false)
    private LocalDateTime horaInicio;

    private LocalDateTime horaFin;

    // Método factory estático: + crear(horaInicio, e: EstadoViaje)
    public static CambioEstado crear(LocalDateTime horaInicio, EstadoViaje e) {
        return CambioEstado.builder()
                .estado(e)
                .horaInicio(horaInicio)
                .build();
    }

    // Método: + tomarFechaYHoraFin(horaFin)
    public void tomarFechaYHoraFin(LocalDateTime horaFin) {
        this.horaFin = horaFin;
    }
}