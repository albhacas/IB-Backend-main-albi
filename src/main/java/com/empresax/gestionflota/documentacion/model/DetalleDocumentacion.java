package com.empresax.gestionflota.documentacion.model;

import com.empresax.gestionflota.chofer.model.Chofer;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DetalleDocumentacion")
@Getter
@Setter
@NoArgsConstructor
public class DetalleDocumentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "estado_documentacion_id")
    private EstadoDocumentacion estadoDocumentacion;

    @OneToMany(mappedBy = "detalleDocumentacion", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chofer_id")
    @JsonBackReference("chofer-documentacion")
    private Chofer chofer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    @JsonBackReference("vehiculo-documentacion")
    private Vehiculo vehiculo;

    @PrePersist
    public void crear() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
        if (fechaExpiracion == null) {
            throw new IllegalArgumentException("La fecha de expiración de la documentación es obligatoria.");
        }
        if (fechaExpiracion.isBefore(fechaEmision)) {
            throw new IllegalArgumentException("La fecha de expiración no puede ser anterior a la fecha de emisión.");
        }
        evaluarEstado();
    }

    public void evaluarEstado() {
        if (fechaExpiracion == null) {
            throw new IllegalArgumentException("La fecha de expiración de la documentación es obligatoria.");
        }
        if (fechaEmision != null && fechaExpiracion.isBefore(fechaEmision)) {
            throw new IllegalArgumentException("La fecha de expiración no puede ser anterior a la fecha de emisión.");
        }
        EstadoDocumentacion estadoActual = estadoDocumentacion != null
                ? estadoDocumentacion
                : new EsVigente();
        estadoActual.evaluarEstado(this);
    }

    public void cambioEstado(EstadoDocumentacion nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado documental es obligatorio.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (!cambiosEstado.isEmpty()) {
            CambioEstadoDocumentacion anterior = cambiosEstado.get(cambiosEstado.size() - 1);
            if (anterior.getHoraFin() == null) {
                anterior.tomarFechaYHoraFin(ahora);
            }
        }

        CambioEstadoDocumentacion cambio = nuevoEstado.crearCambioEstadoDocumentacion(ahora, nuevoEstado);
        cambio.setDetalleDocumentacion(this);
        cambiosEstado.add(cambio);
        estadoDocumentacion = nuevoEstado;
    }

    public EstadoDocumentacion conocerEstadoDocumentacion() {
        return estadoDocumentacion;
    }

    public void asignarChofer(Chofer chofer) {
        this.chofer = chofer;
        this.vehiculo = null;
    }

    public void asignarVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
        this.chofer = null;
    }
}
