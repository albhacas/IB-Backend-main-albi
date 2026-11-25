package com.empresax.gestionflota.viaje.service;

import com.empresax.gestionflota.viaje.dto.PagoAltaDTO;
import com.empresax.gestionflota.viaje.model.Viaje;
import com.empresax.gestionflota.viaje.model.estado.Confirmado;
import com.empresax.gestionflota.viaje.repository.ConceptoPagoRepository;
import com.empresax.gestionflota.viaje.repository.MedioPagoRepository;
import com.empresax.gestionflota.viaje.repository.PagoRepository;
import com.empresax.gestionflota.viaje.repository.ViajeRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PagoServiceTest {

    @Test
    void rechazaRegistrarOtraSeniaSiYaSeRegistroUna() {
        ViajeRepository viajeRepository = mock(ViajeRepository.class);
        PagoRepository pagoRepository = mock(PagoRepository.class);
        MedioPagoRepository medioPagoRepository = mock(MedioPagoRepository.class);
        ConceptoPagoRepository conceptoPagoRepository = mock(ConceptoPagoRepository.class);
        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(new Confirmado())
                .montoTotal(new BigDecimal("1000.00"))
                .montoSenia(new BigDecimal("300.00"))
                .seniaPagada(true)
                .build();
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));

        PagoService service = new PagoService(
                viajeRepository,
                pagoRepository,
                medioPagoRepository,
                conceptoPagoRepository
        );
        PagoAltaDTO dto = PagoAltaDTO.builder()
                .monto(new BigDecimal("100.00"))
                .nombreConcepto("Seña")
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPago(1L, dto)
        );

        assertEquals(
                "La seña de este viaje ya fue registrada. Los próximos cobros deben imputarse al saldo.",
                error.getMessage()
        );
        verifyNoInteractions(pagoRepository, medioPagoRepository, conceptoPagoRepository);
    }
}
