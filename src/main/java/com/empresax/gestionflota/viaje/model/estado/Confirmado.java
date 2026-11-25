package com.empresax.gestionflota.viaje.model.estado;

import com.empresax.gestionflota.viaje.model.CambioEstado;
import com.empresax.gestionflota.viaje.model.Viaje;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("CONFIRMADO")
public class Confirmado extends EstadoViaje {

    public Confirmado() {
        super("Confirmado", "Seña recibida y viaje confirmado");
    }

    @Override
    public void marcarListo(Viaje viaje, CambioEstado actual, BigDecimal monto, LocalDateTime fechaHora) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0
                || (monto.compareTo(BigDecimal.ZERO) == 0
                && viaje.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)) {
            throw new IllegalArgumentException("El pago debe ser mayor a cero.");
        }
        if (monto.compareTo(viaje.getSaldoPendiente()) > 0) {
            throw new IllegalArgumentException("El pago no puede superar el saldo pendiente.");
        }

        BigDecimal montoSaldoAcumulado = (viaje.getMontoSaldo() != null ? viaje.getMontoSaldo() : BigDecimal.ZERO)
                .add(monto);
        viaje.setMontoSaldo(montoSaldoAcumulado);

        if (viaje.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
            return;
        }
        viaje.setTotalPagado(true);
        if (!viaje.tieneColectivosYChoferesAsignados()) {
            return;
        }

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
        return new ListoParaSalir();
    }
}