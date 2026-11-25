package com.empresax.gestionflota.viaje.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "AgendaViaje",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_agenda_anio_mes", columnNames = {"anio", "mes"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class AgendaViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private LocalDate fechaCreacion;

    @Column(nullable = true)
    private String observaciones;

    @OneToMany(mappedBy = "agendaViaje", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Viaje> viajes = new ArrayList<>();

    public AgendaViaje(Long id) {
        this.id = id;
    }

    public void agregarViaje(Viaje viaje) {
        Objects.requireNonNull(viaje, "El viaje no puede ser null");

        AgendaViaje agendaAnterior = viaje.getAgendaViaje();
        if (agendaAnterior != null && agendaAnterior != this) {
            agendaAnterior.quitarViaje(viaje);
        }

        if (!viajes.contains(viaje)) {
            viajes.add(viaje);
        }
        viaje.setAgendaViaje(this);
    }

    public void quitarViaje(Viaje viaje) {
        if (viaje == null) {
            return;
        }

        viajes.remove(viaje);
        if (viaje.getAgendaViaje() == this) {
            viaje.setAgendaViaje(null);
        }
    }

    public Optional<Viaje> consultarViaje(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return viajes.stream()
                .filter(viaje -> Objects.equals(viaje.getId(), id))
                .findFirst();
    }
} 