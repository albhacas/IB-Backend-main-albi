package com.empresax.gestionflota.viaje.model.estado;

import com.empresax.gestionflota.viaje.model.CambioEstado;
import com.empresax.gestionflota.viaje.model.Viaje;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("EN_CURSO")
public class EnCurso extends EstadoViaje {

    public EnCurso() {
        super("EnCurso", "Viaje en tránsito");
    }

    @Override
    public void interrumpir(Viaje viaje, CambioEstado actual, LocalDateTime fechaHora) {
        if (actual != null) {
            actual.tomarFechaYHoraFin(fechaHora);
        }
        EstadoViaje interrumpido = new Interrumpido();
        viaje.tomarEstadoActual(interrumpido);
        viaje.agregarCambioEstado(crearCambioEstado(fechaHora, interrumpido));
    }

    @Override
    public void finalizar(Viaje viaje, CambioEstado actual, LocalDateTime fechaHora) {
        if (actual != null) {
            actual.tomarFechaYHoraFin(fechaHora);
        }
        EstadoViaje proximo = crearProximoEstado();
        viaje.tomarEstadoActual(proximo);
        viaje.agregarCambioEstado(crearCambioEstado(fechaHora, proximo));
    }

    @Override
    public EstadoViaje crearProximoEstado() {
        return new Finalizado();
    }
}