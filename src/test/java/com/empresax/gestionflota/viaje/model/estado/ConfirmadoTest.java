package com.empresax.gestionflota.viaje.model.estado;

import com.empresax.gestionflota.viaje.model.AsignacionChofer;
import com.empresax.gestionflota.viaje.model.UnidadViaje;
import com.empresax.gestionflota.viaje.model.Viaje;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmadoTest {

    @Test
    void permiteRegistrarVariosPagosParciales() {
        Viaje viaje = crearViajeConfirmado();

        viaje.registrarCobroSaldo(new BigDecimal("250.00"), LocalDateTime.now());
        viaje.registrarCobroSaldo(new BigDecimal("200.00"), LocalDateTime.now());

        assertEquals("Confirmado", viaje.getEstado().getNombre());
        assertEquals(new BigDecimal("250.00"), viaje.getSaldoPendiente());
        assertEquals(new BigDecimal("450.00"), viaje.getMontoSaldo());
    }

    @Test
    void requierePagoCompletoColectivoYChoferesAntesDeHabilitarSalida() {
        Viaje viaje = crearViajeConfirmado();

        viaje.registrarCobroSaldo(new BigDecimal("700.00"), LocalDateTime.now());

        assertEquals("Confirmado", viaje.getEstado().getNombre());
        assertTrue(viaje.estaTotalmentePagado());

        UnidadViaje unidad = UnidadViaje.builder()
                .empresaTercera("Transporte de prueba")
                .build();
        viaje.addUnidad(unidad);
        viaje.registrarCobroSaldo(BigDecimal.ZERO, LocalDateTime.now());
        assertEquals("Confirmado", viaje.getEstado().getNombre());

        unidad.addChofer(AsignacionChofer.builder()
                .nombreTercero("Chofer de prueba")
                .build());
        viaje.registrarCobroSaldo(BigDecimal.ZERO, LocalDateTime.now());

        assertEquals("ListoParaSalir", viaje.getEstado().getNombre());
    }

    @Test
    void permiteCompletarElSaldoCuandoElViajeListoTeniaUnPagoIncompleto() {
        Viaje viaje = crearViajeConfirmado();
        viaje.setMontoSaldo(new BigDecimal("600.00"));
        viaje.setTotalPagado(false);
        viaje.tomarEstadoActual(new ListoParaSalir());

        viaje.registrarCobroSaldo(new BigDecimal("100.00"), LocalDateTime.now());

        assertEquals("ListoParaSalir", viaje.getEstado().getNombre());
        assertEquals(0, viaje.getSaldoPendiente().compareTo(BigDecimal.ZERO));
        assertTrue(viaje.estaTotalmentePagado());
    }

    @Test
    void vuelveAConfirmadoSiUnPagoNoCompletaElSaldoDeUnViajeListo() {
        Viaje viaje = crearViajeConfirmado();
        viaje.setMontoSaldo(new BigDecimal("600.00"));
        viaje.setTotalPagado(false);
        viaje.tomarEstadoActual(new ListoParaSalir());

        viaje.registrarCobroSaldo(new BigDecimal("50.00"), LocalDateTime.now());

        assertEquals("Confirmado", viaje.getEstado().getNombre());
        assertEquals(new BigDecimal("50.00"), viaje.getSaldoPendiente());
        assertEquals(new BigDecimal("650.00"), viaje.getMontoSaldo());
    }

    private Viaje crearViajeConfirmado() {
        return Viaje.builder()
                .estado(new Confirmado())
                .montoTotal(new BigDecimal("1000.00"))
                .montoSenia(new BigDecimal("300.00"))
                .seniaPagada(true)
                .montoSaldo(BigDecimal.ZERO)
                .totalPagado(false)
                .build();
    }
}
