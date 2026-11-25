package com.empresax.gestionflota.viaje.service;

import com.empresax.gestionflota.chofer.model.Chofer;
import com.empresax.gestionflota.chofer.service.ChoferService;
import com.empresax.gestionflota.cliente.model.Cliente;
import com.empresax.gestionflota.cliente.repository.ClienteRepository;
import com.empresax.gestionflota.notificacion.service.NotificationService;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.vehiculo.service.VehiculoService;
import com.empresax.gestionflota.viaje.dto.AsignacionChoferDTO;
import com.empresax.gestionflota.viaje.dto.UnidadViajeDTO;
import com.empresax.gestionflota.viaje.dto.ViajeDTO;
import com.empresax.gestionflota.viaje.model.TipoUnidad;
import com.empresax.gestionflota.viaje.model.TipoViaje;
import com.empresax.gestionflota.viaje.model.UnidadViaje;
import com.empresax.gestionflota.viaje.model.Viaje;
import com.empresax.gestionflota.viaje.model.estado.Confirmado;
import com.empresax.gestionflota.viaje.model.estado.EnCurso;
import com.empresax.gestionflota.viaje.model.estado.Interrumpido;
import com.empresax.gestionflota.viaje.repository.TipoUnidadRepository;
import com.empresax.gestionflota.viaje.repository.TipoViajeRepository;
import com.empresax.gestionflota.viaje.repository.ViajeRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViajeServiceTest {

    @Test
    void asignaClienteAlCrearViaje() {
        ViajeRepository viajeRepository = mock(ViajeRepository.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        Cliente cliente = Cliente.builder().id(7L).build();
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.save(any(Viaje.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ViajeService service = new ViajeService(
                viajeRepository,
                clienteRepository,
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                mock(NotificationService.class)
        );
        ViajeDTO dto = ViajeDTO.builder()
                .fechaInicio(LocalDate.now())
                .origen("Buenos Aires")
                .destino("Mendoza")
                .clienteId(7L)
                .build();

        Viaje creado = service.crearViaje(dto);

        assertEquals(cliente, creado.getCliente());
    }

    @Test
    void asignaClienteAlActualizarViaje() {
        ViajeRepository viajeRepository = mock(ViajeRepository.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        Cliente cliente = Cliente.builder().id(8L).build();
        Viaje viaje = Viaje.builder()
                .id(1L)
                .montoTotal(new BigDecimal("1000.00"))
                .build();
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(viajeRepository.save(any(Viaje.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clienteRepository.findById(8L)).thenReturn(Optional.of(cliente));
        ViajeService service = new ViajeService(
                viajeRepository,
                clienteRepository,
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                mock(NotificationService.class)
        );
        ViajeDTO dto = ViajeDTO.builder()
                .fechaInicio(LocalDate.now())
                .montoTotal(new BigDecimal("1000.00"))
                .clienteId(8L)
                .build();

        Viaje actualizado = service.actualizarViaje(1L, dto);

        assertEquals(cliente, actualizado.getCliente());
    }

    @Test
    void rechazaClienteInexistenteAlCrearViaje() {
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        ViajeService service = new ViajeService(
                mock(ViajeRepository.class),
                clienteRepository,
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                mock(NotificationService.class)
        );
        ViajeDTO dto = ViajeDTO.builder()
                .fechaInicio(LocalDate.now())
                .origen("Buenos Aires")
                .destino("Mendoza")
                .clienteId(99L)
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearViaje(dto)
        );

        assertEquals("No se encontró el cliente con ID: 99", error.getMessage());
    }

    @Test
    void interrumpirViajeGuardaElCambioYNotificaQueSeRequiereAuxilio() {
        ViajeRepository viajeRepository = mock(ViajeRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        Viaje viaje = Viaje.builder()
                .id(42L)
                .destino("Mendoza")
                .estado(new EnCurso())
                .build();
        when(viajeRepository.findById(42L)).thenReturn(Optional.of(viaje));
        when(viajeRepository.save(any(Viaje.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ViajeService service = new ViajeService(
                viajeRepository,
                mock(ClienteRepository.class),
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                notificationService
        );

        Viaje interrumpido = service.interrumpirViaje(42L);

        assertEquals("Interrumpido", interrumpido.getEstado().getNombre());
        verify(viajeRepository).save(viaje);
        verify(notificationService).enviarNotificacionATodos(
                "⚠️ ALERTA: Viaje #42 Interrumpido",
                "El móvil reportó desperfecto en trayecto a Mendoza. Se requiere auxilio.",
                "/viajes"
        );
    }

    @Test
    void rechazaColectivoDuplicadoEnElMismoViaje() {
        ViajeService service = new ViajeService(
                mock(ViajeRepository.class),
                mock(ClienteRepository.class),
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                mock(NotificationService.class)
        );
        ViajeDTO dto = ViajeDTO.builder()
                .unidades(List.of(
                        UnidadViajeDTO.builder().vehiculoId(10L).build(),
                        UnidadViajeDTO.builder().vehiculoId(10L).build()
                ))
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearViaje(dto)
        );

        assertEquals("Un mismo colectivo no puede asignarse más de una vez al viaje.", error.getMessage());
    }

    @Test
    void rechazaChoferDuplicadoEnElMismoViaje() {
        ViajeService service = new ViajeService(
                mock(ViajeRepository.class),
                mock(ClienteRepository.class),
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                mock(NotificationService.class)
        );
        AsignacionChoferDTO asignacion1 = AsignacionChoferDTO.builder().choferId(20L).build();
        AsignacionChoferDTO asignacion2 = AsignacionChoferDTO.builder().choferId(20L).build();
        ViajeDTO dto = ViajeDTO.builder()
                .unidades(List.of(
                        UnidadViajeDTO.builder().choferes(List.of(asignacion1)).build(),
                        UnidadViajeDTO.builder().choferes(List.of(asignacion2)).build()
                ))
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearViaje(dto)
        );

        assertEquals("Un mismo chofer no puede asignarse más de una vez al viaje.", error.getMessage());
    }

    @Test
    void rechazaDniDuplicadoEntreChoferesTerceros() {
        ViajeService service = new ViajeService(
                mock(ViajeRepository.class),
                mock(ClienteRepository.class),
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                mock(NotificationService.class)
        );
        ViajeDTO dto = ViajeDTO.builder()
                .unidades(List.of(
                        UnidadViajeDTO.builder().choferes(List.of(
                                AsignacionChoferDTO.builder().dniTercero("12.345.678").build()
                        )).build(),
                        UnidadViajeDTO.builder().choferes(List.of(
                                AsignacionChoferDTO.builder().dniTercero("12345678").build()
                        )).build()
                ))
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearViaje(dto)
        );

        assertEquals("Un mismo chofer tercero no puede asignarse más de una vez al viaje.", error.getMessage());
    }

    @Test
    void rechazaHoraDeLlegadaAnteriorALaSalidaAlCrearViaje() {
        ViajeService service = new ViajeService(
                mock(ViajeRepository.class),
                mock(ClienteRepository.class),
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                mock(NotificationService.class)
        );
        LocalDate hoy = LocalDate.now();
        ViajeDTO dto = ViajeDTO.builder()
                .fechaInicio(hoy)
                .fechaFin(hoy)
                .horaInicio(LocalTime.of(15, 0))
                .horaFin(LocalTime.of(14, 0))
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearViaje(dto)
        );

        assertEquals(
                "La fecha y hora de llegada no puede ser anterior a la fecha y hora de salida.",
                error.getMessage()
        );
    }

    @Test
    void rechazaHoraDeLlegadaAnteriorALaSalidaAlActualizarViaje() {
        ViajeRepository viajeRepository = mock(ViajeRepository.class);
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(Viaje.builder().id(1L).build()));
        ViajeService service = new ViajeService(
                viajeRepository,
                mock(ClienteRepository.class),
                mock(VehiculoService.class),
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                mock(TipoUnidadRepository.class),
                mock(NotificationService.class)
        );
        LocalDate hoy = LocalDate.now();
        ViajeDTO dto = ViajeDTO.builder()
                .fechaInicio(hoy)
                .fechaFin(hoy)
                .horaInicio(LocalTime.of(15, 0))
                .horaFin(LocalTime.of(14, 0))
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarViaje(1L, dto)
        );

        assertEquals(
                "La fecha y hora de llegada no puede ser anterior a la fecha y hora de salida.",
                error.getMessage()
        );
    }

    @Test
    void pasaAListoParaSalirAlAsignarRecursosAUnViajeYaPagado() {
        ViajeRepository viajeRepository = mock(ViajeRepository.class);
        VehiculoService vehiculoService = mock(VehiculoService.class);
        ChoferService choferService = mock(ChoferService.class);
        TipoViajeRepository tipoViajeRepository = mock(TipoViajeRepository.class);
        TipoUnidadRepository tipoUnidadRepository = mock(TipoUnidadRepository.class);

        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(new Confirmado())
                .montoTotal(new BigDecimal("1000.00"))
                .montoSenia(new BigDecimal("300.00"))
                .montoSaldo(new BigDecimal("700.00"))
                .seniaPagada(true)
                .totalPagado(true)
                .build();

        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(viajeRepository.save(any(Viaje.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tipoViajeRepository.findByNombre("Nacional"))
                .thenReturn(Optional.of(TipoViaje.builder().nombre("Nacional").build()));
        when(tipoUnidadRepository.findByNombre("Propia"))
                .thenReturn(Optional.of(TipoUnidad.builder().nombre("Propia").build()));
        when(vehiculoService.buscarPorId(10L))
                .thenReturn(Vehiculo.builder().id(10L).build());
        when(choferService.buscarPorId(20L))
                .thenReturn(Chofer.builder().id(20L).build());

        UnidadViajeDTO unidad = UnidadViajeDTO.builder()
                .nombreTipoUnidad("Propia")
                .vehiculoId(10L)
                .choferes(List.of(AsignacionChoferDTO.builder()
                        .rol("Principal")
                        .choferId(20L)
                        .build()))
                .build();
        ViajeDTO dto = ViajeDTO.builder()
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now())
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(18, 0))
                .nombreTipoViaje("Nacional")
                .montoTotal(new BigDecimal("1000.00"))
                .unidades(List.of(unidad))
                .build();

        Viaje actualizado = new ViajeService(
                viajeRepository,
                mock(ClienteRepository.class),
                vehiculoService,
                choferService,
                tipoViajeRepository,
                tipoUnidadRepository,
                mock(NotificationService.class)
        ).actualizarViaje(1L, dto);

        assertEquals("ListoParaSalir", actualizado.getEstado().getNombre());
        assertEquals(new BigDecimal("700.00"), actualizado.getMontoSaldo());
    }

    @Test
    void reanudaConElMismoColectivoSinDuplicarLaUnidad() {
        ViajeRepository viajeRepository = mock(ViajeRepository.class);
        VehiculoService vehiculoService = mock(VehiculoService.class);
        TipoUnidadRepository tipoUnidadRepository = mock(TipoUnidadRepository.class);
        Vehiculo colectivo = Vehiculo.builder().id(10L).build();
        TipoUnidad tipoAuxilioPersistido = TipoUnidad.builder().id(30L).nombre("Auxilio / Reemplazo").build();
        UnidadViaje unidadOriginal = UnidadViaje.builder()
                .id(40L)
                .tipo(TipoUnidad.builder().id(20L).nombre("Propia").build())
                .vehiculo(colectivo)
                .build();
        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(new Interrumpido())
                .montoTotal(new BigDecimal("1000.00"))
                .montoSenia(new BigDecimal("300.00"))
                .montoSaldo(new BigDecimal("700.00"))
                .seniaPagada(true)
                .totalPagado(true)
                .unidades(new java.util.ArrayList<>(List.of(unidadOriginal)))
                .build();

        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(viajeRepository.save(any(Viaje.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehiculoService.buscarPorId(10L)).thenReturn(colectivo);
        when(tipoUnidadRepository.findByNombre("Auxilio / Reemplazo"))
                .thenReturn(Optional.of(tipoAuxilioPersistido));

        Viaje reanudado = new ViajeService(
                viajeRepository,
                mock(ClienteRepository.class),
                vehiculoService,
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                tipoUnidadRepository,
                mock(NotificationService.class)
        ).continuarViaje(1L, com.empresax.gestionflota.viaje.dto.TransicionEstadoDTO.builder()
                .nuevoVehiculoId(10L)
                .build());

        assertEquals("EnCurso", reanudado.getEstado().getNombre());
        assertEquals(1, reanudado.getUnidades().size());
        assertEquals(unidadOriginal, reanudado.getUnidades().get(0));
    }

    @Test
    void usaElTipoDeAuxilioPersistidoAlAgregarUnColectivoNuevo() {
        ViajeRepository viajeRepository = mock(ViajeRepository.class);
        VehiculoService vehiculoService = mock(VehiculoService.class);
        TipoUnidadRepository tipoUnidadRepository = mock(TipoUnidadRepository.class);
        TipoUnidad tipoAuxilioPersistido = TipoUnidad.builder().id(30L).nombre("Auxilio / Reemplazo").build();
        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(new Interrumpido())
                .montoTotal(new BigDecimal("1000.00"))
                .montoSenia(new BigDecimal("300.00"))
                .montoSaldo(new BigDecimal("700.00"))
                .seniaPagada(true)
                .totalPagado(true)
                .build();

        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(viajeRepository.save(any(Viaje.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehiculoService.buscarPorId(10L))
                .thenReturn(Vehiculo.builder().id(10L).build());
        when(tipoUnidadRepository.findByNombre("Auxilio / Reemplazo"))
                .thenReturn(Optional.of(tipoAuxilioPersistido));

        Viaje reanudado = new ViajeService(
                viajeRepository,
                mock(ClienteRepository.class),
                vehiculoService,
                mock(ChoferService.class),
                mock(TipoViajeRepository.class),
                tipoUnidadRepository,
                mock(NotificationService.class)
        ).continuarViaje(1L, com.empresax.gestionflota.viaje.dto.TransicionEstadoDTO.builder()
                .nuevoVehiculoId(10L)
                .build());

        assertEquals("EnCurso", reanudado.getEstado().getNombre());
        assertEquals(1, reanudado.getUnidades().size());
        assertEquals(tipoAuxilioPersistido, reanudado.getUnidades().get(0).getTipo());
    }
}