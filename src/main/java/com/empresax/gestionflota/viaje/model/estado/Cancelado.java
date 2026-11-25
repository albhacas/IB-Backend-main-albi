package com.empresax.gestionflota.viaje.model.estado;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CANCELADO")
public class Cancelado extends EstadoViaje {
    public Cancelado() {
        super("Cancelado", "Viaje cancelado");
    }
    @Override public EstadoViaje crearProximoEstado() { return null; }
}