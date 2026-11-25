package com.empresax.gestionflota.viaje.model.estado;

import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.viaje.model.CambioEstado;
import com.empresax.gestionflota.viaje.model.TipoUnidad;
import com.empresax.gestionflota.viaje.model.Viaje;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("INTERRUMPIDO")
public class Interrumpido extends EstadoViaje {

    public Interrumpido() {
        super("Interrumpido", "Interrumpido por desperfecto mecánico o contingencia");
    }

    @Override
    public void continuar(Viaje viaje, CambioEstado actual, Vehiculo nuevoVehiculo, TipoUnidad tipoAuxilio, LocalDateTime fechaHora) {
        if (!viaje.estaTotalmentePagado()) {
            throw new IllegalStateException("Debe registrar el pago total del monto estimado antes de continuar el viaje.");
        }
        if (nuevoVehiculo != null) {
            viaje.asignarNuevoColectivo(nuevoVehiculo, tipoAuxilio);
        }
        if (actual != null) {
            actual.tomarFechaYHoraFin(fechaHora);
        }
        EstadoViaje proximo = crearProximoEstado();
        viaje.tomarEstadoActual(proximo);
        viaje.agregarCambioEstado(crearCambioEstado(fechaHora, proximo));
    }

    @Override
    public EstadoViaje crearProximoEstado() {
        return new EnCurso();
    }
}