package com.empresax.gestionflota.documentacion.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "EstadoDocumentacion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_estado")
@Getter
@Setter
@NoArgsConstructor
public abstract class EstadoDocumentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    protected EstadoDocumentacion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public abstract EstadoDocumentacion crearProximoEstado();

    public CambioEstadoDocumentacion crearCambioEstadoDocumentacion(
            LocalDateTime horario, EstadoDocumentacion estado) {
        return CambioEstadoDocumentacion.crear(horario, estado);
    }

    public void evaluarEstado(DetalleDocumentacion detalle) {
        if (detalle == null || detalle.getFechaExpiracion() == null) {
            throw new IllegalArgumentException("La fecha de expiración de la documentación es obligatoria.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime vencimiento = detalle.getFechaExpiracion();
        EstadoDocumentacion estadoEsperado;
        if (!vencimiento.isAfter(ahora)) {
            estadoEsperado = new EsNoVigente();
        } else if (!vencimiento.isAfter(ahora.plusDays(30))) {
            estadoEsperado = new EsPorVencer();
        } else {
            estadoEsperado = new EsVigente();
        }

        if (detalle.getEstadoDocumentacion() == null
                || !detalle.getEstadoDocumentacion().getClass().equals(estadoEsperado.getClass())) {
            detalle.cambioEstado(estadoEsperado);
        }
    }
}
