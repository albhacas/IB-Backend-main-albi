package com.empresax.gestionflota.documentacion.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CambioEstadoDocumentacion")
@Getter
@Setter
@NoArgsConstructor
public class CambioEstadoDocumentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime horaInicio;

    private LocalDateTime horaFin;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "estado_documentacion_id", nullable = false)
    private EstadoDocumentacion estadoDocumentacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_documentacion_id", nullable = false)
    @JsonBackReference
    private DetalleDocumentacion detalleDocumentacion;

    public static CambioEstadoDocumentacion crear(
            LocalDateTime horaInicio, EstadoDocumentacion estadoDocumentacion) {
        if (horaInicio == null || estadoDocumentacion == null) {
            throw new IllegalArgumentException("El inicio y el estado documental son obligatorios.");
        }

        CambioEstadoDocumentacion cambio = new CambioEstadoDocumentacion();
        cambio.setHoraInicio(horaInicio);
        cambio.setEstadoDocumentacion(estadoDocumentacion);
        return cambio;
    }

    public void tomarFechaYHoraFin(LocalDateTime horaFin) {
        if (horaFin == null || horaFin.isBefore(horaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior al inicio del estado.");
        }
        this.horaFin = horaFin;
    }

    public EstadoDocumentacion conocerEstadoDocumentacion() {
        return estadoDocumentacion;
    }
}
