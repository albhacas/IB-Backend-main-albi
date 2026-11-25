package com.empresax.gestionflota.viaje.model;

import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "UnidadViaje")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UnidadViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_unidad_id", nullable = false)
    private TipoUnidad tipo;

    private String empresaTercera;
    private Integer cantidadColectivosTercero;
    private String patenteTercero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    @JsonBackReference
    private Viaje viaje;

    // Relación con Vehiculo (Sprint 1)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @OneToMany(mappedBy = "unidadViaje", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<AsignacionChofer> choferes = new ArrayList<>();

    public TipoUnidad conocerTipoUnidad() {
        return this.tipo;
    }

    public Vehiculo conocerVehiculo() {
        return this.vehiculo;
    }

    public void addChofer(AsignacionChofer asig) {
        choferes.add(asig);
        asig.setUnidadViaje(this);
    }
}