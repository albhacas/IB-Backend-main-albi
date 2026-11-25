package com.empresax.gestionflota.viaje.model.estado;

import com.empresax.gestionflota.viaje.model.CambioEstado;
import com.empresax.gestionflota.viaje.model.Viaje;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("LISTO_PARA_SALIR")
public class ListoParaSalir extends EstadoViaje {

    public ListoParaSalir() {
        super("ListoParaSalir", "Unidades asignadas y saldo pagado");
    }

    @Override
    public void marcarListo(Viaje viaje, CambioEstado actual, BigDecimal monto, LocalDateTime fechaHora) {
        BigDecimal saldoPendiente = viaje.getSaldoPendiente();
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El pago debe ser mayor a cero.");
        }
        if (monto.compareTo(saldoPendiente) > 0) {
            throw new IllegalArgumentException("El pago no puede superar el saldo pendiente.");
        }

        BigDecimal montoSaldoAcumulado = (viaje.getMontoSaldo() != null ? viaje.getMontoSaldo() : BigDecimal.ZERO)
                .add(monto);
        viaje.setMontoSaldo(montoSaldoAcumulado);

        if (viaje.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            viaje.setTotalPagado(true);
            return;
        }

        viaje.setTotalPagado(false);
        if (actual != null) {
            actual.tomarFechaYHoraFin(fechaHora);
        }
        EstadoViaje confirmado = new Confirmado();
        viaje.tomarEstadoActual(confirmado);
        viaje.agregarCambioEstado(crearCambioEstado(fechaHora, confirmado));
    }

    @Override
    public void iniciar(Viaje viaje, CambioEstado actual, LocalDateTime fechaHora) {
        if (!viaje.estaTotalmentePagado()) {
            throw new IllegalStateException("Debe registrar el pago total del monto estimado antes de iniciar el viaje.");
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