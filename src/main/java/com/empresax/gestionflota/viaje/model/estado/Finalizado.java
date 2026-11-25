package com.empresax.gestionflota.viaje.model.estado;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FINALIZADO")
public class Finalizado extends EstadoViaje {
    public Finalizado() {
        super("Finalizado", "Viaje completado exitosamente");
    }
    @Override public EstadoViaje crearProximoEstado() { return null; }
}