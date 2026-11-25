package com.empresax.gestionflota.viaje.model.estado;

import com.empresax.gestionflota.viaje.model.CambioEstado;
import com.empresax.gestionflota.viaje.model.Viaje;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("PROGRAMADO")
public class Programado extends EstadoViaje {

    public Programado() {
        super("Programado", "Viaje creado pendiente de seña");
    }

    @Override
    public void registrarSenia(Viaje viaje, CambioEstado actual, BigDecimal monto, LocalDateTime fechaHora) {
        viaje.setMontoSenia(monto);
        viaje.setSeniaPagada(true);
        viaje.setTotalPagado(viaje.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0);

        if (actual != null) {
            actual.tomarFechaYHoraFin(fechaHora);
        }

        EstadoViaje proximo = crearProximoEstado();
        viaje.tomarEstadoActual(proximo);
        viaje.agregarCambioEstado(crearCambioEstado(fechaHora, proximo));
    }

    @Override
    public void cancelar(Viaje viaje, CambioEstado actual, LocalDateTime fechaHora) {
        if (actual != null) {
            actual.tomarFechaYHoraFin(fechaHora);
        }
        EstadoViaje cancelado = new Cancelado();
        viaje.tomarEstadoActual(cancelado);
        viaje.agregarCambioEstado(crearCambioEstado(fechaHora, cancelado));
    }

    @Override
    public EstadoViaje crearProximoEstado() {
        return new Confirmado();
    }
}