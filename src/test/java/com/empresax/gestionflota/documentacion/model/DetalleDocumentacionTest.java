package com.empresax.gestionflota.documentacion.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DetalleDocumentacionTest {

    @Test
    void creaDocumentacionVigenteConSuPrimerCambioDeEstado() {
        DetalleDocumentacion detalle = new DetalleDocumentacion();
        detalle.setFechaExpiracion(LocalDateTime.now().plusDays(60));

        detalle.crear();

        assertNotNull(detalle.getFechaEmision());
        assertEquals(EsVigente.class, detalle.getEstadoDocumentacion().getClass());
        assertEquals(1, detalle.getCambiosEstado().size());
        assertEquals(detalle, detalle.getCambiosEstado().get(0).getDetalleDocumentacion());
    }

    @Test
    void cambiaAEstadoPorVencerDentroDeLosTreintaDias() {
        DetalleDocumentacion detalle = new DetalleDocumentacion();
        detalle.setFechaExpiracion(LocalDateTime.now().plusDays(10));

        detalle.crear();

        assertEquals(EsPorVencer.class, detalle.getEstadoDocumentacion().getClass());
    }

    @Test
    void cierraEstadoAnteriorCuandoLaDocumentacionVence() {
        DetalleDocumentacion detalle = new DetalleDocumentacion();
        detalle.setFechaExpiracion(LocalDateTime.now().plusDays(10));
        detalle.crear();
        CambioEstadoDocumentacion estadoAnterior = detalle.getCambiosEstado().get(0);
        detalle.setFechaExpiracion(LocalDateTime.now().minusDays(1));

        detalle.evaluarEstado();

        assertEquals(EsNoVigente.class, detalle.getEstadoDocumentacion().getClass());
        assertNotNull(estadoAnterior.getHoraFin());
        assertEquals(2, detalle.getCambiosEstado().size());
    }
}
