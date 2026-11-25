package com.empresax.gestionflota.viaje.service;

import com.empresax.gestionflota.chofer.model.Chofer;
import com.empresax.gestionflota.chofer.service.ChoferService;
import com.empresax.gestionflota.cliente.model.Cliente;
import com.empresax.gestionflota.cliente.repository.ClienteRepository;
import com.empresax.gestionflota.notificacion.service.NotificationService;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.vehiculo.service.VehiculoService;
import com.empresax.gestionflota.viaje.dto.AsignacionChoferDTO;
import com.empresax.gestionflota.viaje.dto.ModificarAgendaDTO;
import com.empresax.gestionflota.viaje.dto.TransicionEstadoDTO;
import com.empresax.gestionflota.viaje.dto.UnidadViajeDTO;
import com.empresax.gestionflota.viaje.dto.ViajeDTO;
import com.empresax.gestionflota.viaje.model.AsignacionChofer;
import com.empresax.gestionflota.viaje.model.CambioEstado;
import com.empresax.gestionflota.viaje.model.TipoUnidad;
import com.empresax.gestionflota.viaje.model.TipoViaje;
import com.empresax.gestionflota.viaje.model.UnidadViaje;
import com.empresax.gestionflota.viaje.model.Viaje;
import com.empresax.gestionflota.viaje.model.estado.Confirmado;
import com.empresax.gestionflota.viaje.model.estado.EstadoViaje;
import com.empresax.gestionflota.viaje.model.estado.Programado;
import com.empresax.gestionflota.viaje.repository.TipoUnidadRepository;
import com.empresax.gestionflota.viaje.repository.TipoViajeRepository;
import com.empresax.gestionflota.viaje.repository.ViajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ViajeService {
    private final ViajeRepository viajeRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoService vehiculoService;
    private final ChoferService choferService;
    private final TipoViajeRepository tipoViajeRepository;
    private final TipoUnidadRepository tipoUnidadRepository;
    private final NotificationService notificationService;

    public ViajeService(ViajeRepository viajeRepository,
                        ClienteRepository clienteRepository,
                        VehiculoService vehiculoService,
                        ChoferService choferService,
                        TipoViajeRepository tipoViajeRepository,
                        TipoUnidadRepository tipoUnidadRepository,
                        NotificationService notificationService) {
        this.viajeRepository = viajeRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoService = vehiculoService;
        this.choferService = choferService;
        this.tipoViajeRepository = tipoViajeRepository;
        this.tipoUnidadRepository = tipoUnidadRepository;
        this.notificationService = notificationService;
    }

    private Cliente resolverCliente(Long clienteId) {
        if (clienteId == null) return null;
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el cliente con ID: " + clienteId));
    }

    @Transactional(readOnly = true)
    public List<Viaje> listarTodos() {
        return viajeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Viaje buscarPorId(Long id) {
        return viajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el viaje #" + id));
    }

    @Transactional
    public Viaje crearViaje(ViajeDTO dto) {
        validarAsignacionesUnicas(dto);
        if (dto.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de salida es obligatoria.");
        }
        validarFechaHoraLlegada(dto);
        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de salida no puede ser anterior al día de hoy.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        EstadoViaje estadoInicial = new Programado();

        String nombreTipo = (dto.getNombreTipoViaje() != null && !dto.getNombreTipoViaje().isBlank())
                ? dto.getNombreTipoViaje()
                : "Nacional";

        TipoViaje tipoViaje = tipoViajeRepository.findByNombre(nombreTipo)
                .orElseGet(() -> tipoViajeRepository.save(
                        TipoViaje.builder()
                                .nombre(nombreTipo)
                                .descripcion("Servicio regular de viaje")
                                .build()
                ));

        Viaje viaje = Viaje.builder()
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .cliente(resolverCliente(dto.getClienteId()))
                .paradas(dto.getParadas())
                .instruccionesRuta(dto.getInstruccionesRuta())
                .esIdaYVuelta(dto.getEsIdaYVuelta() != null ? dto.getEsIdaYVuelta() : false)
                .fechaLlegadaIda(dto.getFechaLlegadaIda())
                .horaLlegadaIda(dto.getHoraLlegadaIda())
                .fechaRegreso(dto.getFechaRegreso())
                .horaRegreso(dto.getHoraRegreso())
                .itinerarioJson(dto.getItinerarioJson())
                .tipo(tipoViaje)
                .estado(estadoInicial)
                .montoTotal(dto.getMontoTotal())
                .montoSenia(dto.getMontoSenia())
                .seniaPagada(false)
                .totalPagado(false)
                .activo(true)
                .build();

        CambioEstado cambioInicial = CambioEstado.crear(ahora, estadoInicial);
        viaje.agregarCambioEstado(cambioInicial);

        if (dto.getUnidades() != null) {
            for (UnidadViajeDTO uDto : dto.getUnidades()) {
                viaje.addUnidad(mapearUnidad(uDto));
            }
        }

        return viajeRepository.save(viaje);
    }

    // --- ABMC: MODIFICACIÓN GENERAL DE VIAJE ---
    @Transactional
    public Viaje actualizarViaje(Long id, ViajeDTO dto) {
        validarAsignacionesUnicas(dto);
        validarFechaHoraLlegada(dto);
        Viaje viaje = buscarPorId(id);

        viaje.setCliente(resolverCliente(dto.getClienteId()));
        viaje.setOrigen(dto.getOrigen());
        viaje.setDestino(dto.getDestino());
        viaje.setFechaInicio(dto.getFechaInicio());
        viaje.setFechaFin(dto.getFechaFin() != null ? dto.getFechaFin() : dto.getFechaInicio());
        viaje.setHoraInicio(dto.getHoraInicio());
        viaje.setHoraFin(dto.getHoraFin());
        if (dto.getParadas() != null) viaje.setParadas(dto.getParadas());
        if (dto.getInstruccionesRuta() != null) viaje.setInstruccionesRuta(dto.getInstruccionesRuta());
        if (dto.getEsIdaYVuelta() != null) viaje.setEsIdaYVuelta(dto.getEsIdaYVuelta());
        if (dto.getFechaLlegadaIda() != null) viaje.setFechaLlegadaIda(dto.getFechaLlegadaIda());
        if (dto.getHoraLlegadaIda() != null) viaje.setHoraLlegadaIda(dto.getHoraLlegadaIda());
        if (dto.getFechaRegreso() != null) viaje.setFechaRegreso(dto.getFechaRegreso());
        if (dto.getHoraRegreso() != null) viaje.setHoraRegreso(dto.getHoraRegreso());
        if (dto.getItinerarioJson() != null) viaje.setItinerarioJson(dto.getItinerarioJson());

        if (dto.getMontoTotal() == null || dto.getMontoTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto total no puede ser negativo.");
        }
        if (viaje.getTotalCobrado().compareTo(dto.getMontoTotal()) > 0) {
            throw new IllegalArgumentException("El monto total no puede ser menor al total ya abonado.");
        }
        if (Boolean.TRUE.equals(viaje.getTotalPagado())
                && viaje.getMontoTotal().compareTo(dto.getMontoTotal()) != 0) {
            throw new IllegalArgumentException("No se puede modificar el monto total de un viaje ya pagado.");
        }

        viaje.setMontoTotal(dto.getMontoTotal());

        if (!Boolean.TRUE.equals(viaje.getSeniaPagada()) && dto.getMontoSenia() != null) {
            viaje.setMontoSenia(dto.getMontoSenia());
        }

        if (dto.getNombreTipoViaje() != null && !dto.getNombreTipoViaje().isBlank()) {
            TipoViaje tipo = tipoViajeRepository.findByNombre(dto.getNombreTipoViaje())
                    .orElseGet(() -> tipoViajeRepository.save(
                            TipoViaje.builder()
                                    .nombre(dto.getNombreTipoViaje())
                                    .descripcion("Servicio regular de viaje")
                                    .build()
                    ));
            viaje.setTipo(tipo);
        }

        if (dto.getUnidades() != null) {
            viaje.getUnidades().clear();
            for (UnidadViajeDTO uDto : dto.getUnidades()) {
                viaje.addUnidad(mapearUnidad(uDto));
            }
        }

        if (viaje.getEstado() instanceof Confirmado
                && viaje.estaTotalmentePagado()
                && viaje.tieneColectivosYChoferesAsignados()) {
            viaje.registrarCobroSaldo(BigDecimal.ZERO, LocalDateTime.now());
        }

        return viajeRepository.save(viaje);
    }

    // --- MODIFICACIÓN DE AGENDA / DRAG & DROP / HOJA DE RUTA ---
    @Transactional
    public Viaje modificarAgenda(Long id, ModificarAgendaDTO dto) {
        Viaje viaje = buscarPorId(id);

        if (dto.getFechaInicio() != null) {
            viaje.setFechaInicio(dto.getFechaInicio());
        }

        if (dto.getFechaFin() != null) {
            viaje.setFechaFin(dto.getFechaFin());
        } else if (dto.getFechaInicio() != null && viaje.getFechaFin() != null && viaje.getFechaFin().isBefore(dto.getFechaInicio())) {
            viaje.setFechaFin(dto.getFechaInicio());
        }

        if (dto.getHoraInicio() != null) {
            viaje.setHoraInicio(dto.getHoraInicio());
        }
        if (dto.getHoraFin() != null) {
            viaje.setHoraFin(dto.getHoraFin());
        }

        // Validación de coherencia de horario
        if (viaje.getFechaFin() != null && viaje.getFechaInicio() != null) {
            if (viaje.getFechaFin().isBefore(viaje.getFechaInicio())) {
                throw new IllegalArgumentException("La fecha de llegada no puede ser anterior a la de salida.");
            }
            if (viaje.getFechaFin().equals(viaje.getFechaInicio())
                    && viaje.getHoraInicio() != null
                    && viaje.getHoraFin() != null
                    && viaje.getHoraFin().isBefore(viaje.getHoraInicio())) {
                throw new IllegalArgumentException("La hora de llegada no puede ser anterior a la de salida.");
            }
        }

        if (dto.getParadas() != null) {
            viaje.setParadas(dto.getParadas());
        } else if (dto.getParadasLista() != null) {
            viaje.setParadas(String.join("; ", dto.getParadasLista()));
        }

        if (dto.getInstruccionesRuta() != null) {
            viaje.setInstruccionesRuta(dto.getInstruccionesRuta());
        }
        if (dto.getEsIdaYVuelta() != null) viaje.setEsIdaYVuelta(dto.getEsIdaYVuelta());
        if (dto.getFechaLlegadaIda() != null) viaje.setFechaLlegadaIda(dto.getFechaLlegadaIda());
        if (dto.getHoraLlegadaIda() != null) viaje.setHoraLlegadaIda(dto.getHoraLlegadaIda());
        if (dto.getFechaRegreso() != null) viaje.setFechaRegreso(dto.getFechaRegreso());
        if (dto.getHoraRegreso() != null) viaje.setHoraRegreso(dto.getHoraRegreso());
        if (dto.getItinerarioJson() != null) viaje.setItinerarioJson(dto.getItinerarioJson());

        return viajeRepository.save(viaje);
    }

    private void validarFechaHoraLlegada(ViajeDTO dto) {
        LocalDate fechaInicio = dto.getFechaInicio();
        LocalDate fechaFin = dto.getFechaFin() != null ? dto.getFechaFin() : fechaInicio;
        if (fechaInicio == null || fechaFin == null) return;

        boolean fechaAnterior = fechaFin.isBefore(fechaInicio);
        boolean horaAnteriorMismoDia = fechaFin.equals(fechaInicio)
                && dto.getHoraInicio() != null
                && dto.getHoraFin() != null
                && dto.getHoraFin().isBefore(dto.getHoraInicio());

        if (fechaAnterior || horaAnteriorMismoDia) {
            throw new IllegalArgumentException(
                    "La fecha y hora de llegada no puede ser anterior a la fecha y hora de salida."
            );
        }
    }

    private void validarAsignacionesUnicas(ViajeDTO dto) {
        if (dto == null || dto.getUnidades() == null) return;
        Set<Long> vehiculos = new HashSet<>();
        Set<Long> choferes = new HashSet<>();
        Set<String> dniChoferesTerceros = new HashSet<>();

        for (UnidadViajeDTO unidad : dto.getUnidades()) {
            if (unidad == null) continue;
            if (unidad.getVehiculoId() != null && unidad.getVehiculoId() > 0
                    && !vehiculos.add(unidad.getVehiculoId())) {
                throw new IllegalArgumentException("Un mismo colectivo no puede asignarse más de una vez al viaje.");
            }
            if (unidad.getChoferes() == null) continue;
            for (AsignacionChoferDTO asignacion : unidad.getChoferes()) {
                if (asignacion == null) continue;
                if (asignacion.getChoferId() != null && asignacion.getChoferId() > 0
                        && !choferes.add(asignacion.getChoferId())) {
                    throw new IllegalArgumentException("Un mismo chofer no puede asignarse más de una vez al viaje.");
                }
                if (asignacion.getDniTercero() != null && !asignacion.getDniTercero().isBlank()) {
                    String dni = asignacion.getDniTercero().replaceAll("\\D", "");
                    if (!dni.isEmpty() && !dniChoferesTerceros.add(dni)) {
                        throw new IllegalArgumentException("Un mismo chofer tercero no puede asignarse más de una vez al viaje.");
                    }
                }
            }
        }
    }

    @Transactional
    public void eliminarViaje(Long id) {
        Viaje viaje = buscarPorId(id);
        viaje.setActivo(false);
        viajeRepository.save(viaje);
    }

    @Transactional
    public Viaje agregarUnidad(Long viajeId, UnidadViajeDTO uDto) {
        Viaje viaje = buscarPorId(viajeId);
        viaje.addUnidad(mapearUnidad(uDto));
        return viajeRepository.save(viaje);
    }

    private UnidadViaje mapearUnidad(UnidadViajeDTO uDto) {
        String nombreTipoUnidad = (uDto.getNombreTipoUnidad() != null && !uDto.getNombreTipoUnidad().isBlank())
                ? uDto.getNombreTipoUnidad()
                : "Propia";

        TipoUnidad tipo = tipoUnidadRepository.findByNombre(nombreTipoUnidad)
                .orElseGet(() -> tipoUnidadRepository.save(
                        TipoUnidad.builder()
                                .nombre(nombreTipoUnidad)
                                .descripcion("Tipo de unidad " + nombreTipoUnidad)
                                .build()
                ));

        UnidadViaje unidad = UnidadViaje.builder()
                .tipo(tipo)
                .empresaTercera(uDto.getEmpresaTercera())
                .cantidadColectivosTercero(uDto.getCantidadColectivosTercero())
                .patenteTercero(uDto.getPatenteTercero())
                .build();

        if (uDto.getVehiculoId() != null && uDto.getVehiculoId() > 0) {
            Vehiculo v = vehiculoService.buscarPorId(uDto.getVehiculoId());
            unidad.setVehiculo(v);
        }

        if (uDto.getChoferes() != null) {
            for (AsignacionChoferDTO cDto : uDto.getChoferes()) {
                AsignacionChofer asig = AsignacionChofer.builder()
                        .rol(cDto.getRol() != null ? cDto.getRol() : "Principal")
                        .fecha(cDto.getFecha() != null ? cDto.getFecha() : LocalDate.now())
                        .nombreTercero(cDto.getNombreTercero())
                        .dniTercero(cDto.getDniTercero())
                        .telefonoTercero(cDto.getTelefonoTercero())
                        .build();

                if (cDto.getChoferId() != null && cDto.getChoferId() > 0) {
                    Chofer ch = choferService.buscarPorId(cDto.getChoferId());
                    asig.setChofer(ch);
                }
                unidad.addChofer(asig);
            }
        }
        return unidad;
    }

    @Transactional
    public Viaje registrarSenia(Long viajeId, TransicionEstadoDTO dto) {
        Viaje viaje = buscarPorId(viajeId);
        BigDecimal monto = (dto != null && dto.getMonto() != null) ? dto.getMonto() : viaje.getSeniaSugerida();
        validarMontoCobro(monto, viaje.getMontoTotal(), "La seña");
        viaje.registrarSenia(monto, LocalDateTime.now());
        return viajeRepository.save(viaje);
    }

    @Transactional
    public Viaje registrarCobroSaldo(Long viajeId, TransicionEstadoDTO dto) {
        Viaje viaje = buscarPorId(viajeId);
        BigDecimal monto = (dto != null && dto.getMonto() != null) ? dto.getMonto() : viaje.getSaldoPendiente();

        boolean saldoYaCancelado = Boolean.TRUE.equals(viaje.getTotalPagado())
                && viaje.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0
                && monto.compareTo(BigDecimal.ZERO) == 0;

        if (!saldoYaCancelado) {
            validarMontoCobro(monto, viaje.getSaldoPendiente(), "El pago");
        }
        viaje.registrarCobroSaldo(monto, LocalDateTime.now());
        return viajeRepository.save(viaje);
    }

    private void validarMontoCobro(BigDecimal monto, BigDecimal limite, String concepto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(concepto + " debe ser mayor a cero.");
        }
        if (limite == null || monto.compareTo(limite) > 0) {
            throw new IllegalArgumentException(concepto + " no puede superar el monto disponible.");
        }
    }

    @Transactional
    public Viaje iniciarViaje(Long viajeId) {
        Viaje viaje = buscarPorId(viajeId);
        viaje.iniciarViaje(LocalDateTime.now());
        Viaje guardado = viajeRepository.save(viaje);
        notificarCambioEstado(guardado, "Viaje #" + guardado.getId() + " Iniciado",
                "El viaje con destino a " + guardado.getDestino() + " inició su recorrido.");
        return guardado;
    }

    @Transactional
    public Viaje interrumpirViaje(Long viajeId) {
        Viaje viaje = buscarPorId(viajeId);
        viaje.interrumpirViaje(LocalDateTime.now());
        Viaje guardado = viajeRepository.save(viaje);
        notificarCambioEstado(guardado, "⚠️ ALERTA: Viaje #" + guardado.getId() + " Interrumpido",
                "El móvil reportó desperfecto en trayecto a " + guardado.getDestino() + ". Se requiere auxilio.");
        return guardado;
    }

    @Transactional
    public Viaje continuarViaje(Long viajeId, TransicionEstadoDTO dto) {
        Viaje viaje = buscarPorId(viajeId);
        Vehiculo nuevoVehiculo = null;
        TipoUnidad tipoAuxilio = null;
        if (dto != null && dto.getNuevoVehiculoId() != null) {
            nuevoVehiculo = vehiculoService.buscarPorId(dto.getNuevoVehiculoId());
            tipoAuxilio = tipoUnidadRepository.findByNombre("Auxilio / Reemplazo")
                    .orElseGet(() -> tipoUnidadRepository.save(TipoUnidad.builder()
                            .nombre("Auxilio / Reemplazo")
                            .descripcion("Tipo de unidad Auxilio / Reemplazo")
                            .build()));
        }
        viaje.continuarViaje(nuevoVehiculo, tipoAuxilio, LocalDateTime.now());
        Viaje guardado = viajeRepository.save(viaje);
        String mensaje = nuevoVehiculo == null
                ? "El viaje con destino a " + guardado.getDestino() + " reanudó su recorrido."
                : "Se asignó una unidad de auxilio/reemplazo al viaje con destino a " + guardado.getDestino() + ".";
        notificarCambioEstado(guardado,
                "Viaje #" + guardado.getId() + (nuevoVehiculo == null ? " Reanudado" : " Auxilio asignado"),
                mensaje);
        return guardado;
    }

    @Transactional
    public Viaje finalizarViaje(Long viajeId) {
        Viaje viaje = buscarPorId(viajeId);
        viaje.finalizarViaje(LocalDateTime.now());
        Viaje guardado = viajeRepository.save(viaje);
        notificarCambioEstado(guardado, "Viaje #" + guardado.getId() + " Finalizado",
                "El viaje con destino a " + guardado.getDestino() + " finalizó.");
        return guardado;
    }

    @Transactional
    public Viaje cancelarViaje(Long viajeId) {
        Viaje viaje = buscarPorId(viajeId);
        viaje.cancelarViaje(LocalDateTime.now());
        Viaje guardado = viajeRepository.save(viaje);
        notificarCambioEstado(guardado, "Viaje #" + guardado.getId() + " Cancelado",
                "El viaje con destino a " + guardado.getDestino() + " fue cancelado.");
        return guardado;
    }

    private void notificarCambioEstado(Viaje viaje, String titulo, String mensaje) {
        Runnable enviarNotificacion = () -> notificationService.enviarNotificacionATodos(
                titulo,
                mensaje,
                "/viajes"
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviarNotificacion.run();
                }
            });
        } else {
            enviarNotificacion.run();
        }
    }
}