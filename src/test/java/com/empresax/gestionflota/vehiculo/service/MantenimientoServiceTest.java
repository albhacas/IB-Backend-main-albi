package com.empresax.gestionflota.vehiculo.service;

import com.empresax.gestionflota.vehiculo.dto.MantenimientoAltaDTO;
import com.empresax.gestionflota.vehiculo.model.Mantenimiento;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.vehiculo.repository.MantenimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MantenimientoServiceTest {

    @Mock
    private MantenimientoRepository mantenimientoRepository;

    @Mock
    private VehiculoService vehiculoService;

    @InjectMocks
    private MantenimientoService mantenimientoService;

    private Vehiculo vehiculo;
    private MantenimientoAltaDTO dto;

    @BeforeEach
    void setUp() {
        vehiculo = Vehiculo.builder()
                .id(1L)
                .interno(104)
                .patente("AF123JK")
                .activo(true)
                .build();

        dto = MantenimientoAltaDTO.builder()
                .vehiculoId(1L)
                .tipoServicio("PREVENTIVO")
                .fechaMantenimiento(LocalDate.now())
                .costo(BigDecimal.valueOf(120000.00))
                .observaciones("Cambio de aceite, filtro y revisión de frenos.")
                .build();
    }

    @Test
    void registrarMantenimiento_Exitoso() {
        when(vehiculoService.buscarPorId(1L)).thenReturn(vehiculo);
        when(mantenimientoRepository.save(any(Mantenimiento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mantenimiento resultado = mantenimientoService.registrarMantenimiento(dto);

        assertNotNull(resultado);
        assertEquals("PREVENTIVO", resultado.getTipoServicio());
        assertEquals(vehiculo, resultado.getVehiculo());
        assertEquals(BigDecimal.valueOf(120000.00), resultado.getCosto());
        assertEquals("Cambio de aceite, filtro y revisión de frenos.", resultado.getObservaciones());

        verify(vehiculoService).buscarPorId(1L);
        verify(mantenimientoRepository).save(any(Mantenimiento.class));
    }

    @Test
    void registrarMantenimiento_VehiculoNoExiste_LanzaExcepcion() {
        when(vehiculoService.buscarPorId(1L))
                .thenThrow(new IllegalArgumentException("No se encontró el vehículo con ID: 1"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mantenimientoService.registrarMantenimiento(dto));

        assertTrue(ex.getMessage().contains("No se encontró el vehículo"));
        verify(mantenimientoRepository, never()).save(any());
    }

    @Test
    void listarTodos_RetornaLista() {
        when(mantenimientoRepository.findAll()).thenReturn(List.of(new Mantenimiento()));

        List<Mantenimiento> lista = mantenimientoService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        verify(mantenimientoRepository).findAll();
    }

    @Test
    void obtenerHistorialPorVehiculo_RetornaLista() {
        when(mantenimientoRepository.findByVehiculoId(1L)).thenReturn(List.of(new Mantenimiento()));

        List<Mantenimiento> historial = mantenimientoService.obtenerHistorialPorVehiculo(1L);

        assertNotNull(historial);
        assertEquals(1, historial.size());
        verify(mantenimientoRepository).findByVehiculoId(1L);
    }
}