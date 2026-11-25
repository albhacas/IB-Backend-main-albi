package com.empresax.gestionflota.viaje.model;

import com.empresax.gestionflota.cliente.model.Cliente;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.viaje.model.estado.EstadoViaje;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Viaje")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Viaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Column(nullable = false)
    private LocalTime horaInicio;
    private LocalTime horaFin;

    @Column(nullable = false, length = 100)
    private String origen;

    @Column(nullable = false, length = 100)
    private String destino;

    @Column(length = 1000)
    private String paradas; // Paradas intermedias de la hoja de ruta

    @Column(length = 1000)
    private String instruccionesRuta; // Instrucciones operativas / logística

    @Builder.Default
    private Boolean esIdaYVuelta = false;

    private LocalDate fechaLlegadaIda;
    private LocalTime horaLlegadaIda;

    private LocalDate fechaRegreso;
    private LocalTime horaRegreso;

    @Column(columnDefinition = "TEXT")
    private String itinerarioJson;

    @ManyToOne
    @JoinColumn(name = "tipo_viaje_id")
    private TipoViaje tipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_actual_id", nullable = false)
    private EstadoViaje estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_viaje_id")
    @JsonBackReference
    private AgendaViaje agendaViaje;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoSenia;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoSaldo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean seniaPagada = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean totalPagado = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @OrderBy("horaInicio DESC")
    @Builder.Default
    private List<CambioEstado> historialEstados = new ArrayList<>();

    @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<UnidadViaje> unidades = new ArrayList<>();

    @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @OrderBy("fechaHora DESC")
    @Builder.Default
    private List<Pago> pagos = new ArrayList<>();

    // --- MÉTODOS DEL DIAGRAMA ---
    public TipoViaje conocerTipoViaje() {
        return this.tipo;
    }

    public EstadoViaje conocerEstado() {
        return this.estado;
    }

    public void tomarEstadoActual(EstadoViaje e) {
        this.estado = e;
    }

    public void agregarCambioEstado(CambioEstado ce) {
        this.historialEstados.add(0, ce);
        ce.setViaje(this);
    }

    private CambioEstado getCambioEstadoActual() {
        return historialEstados.isEmpty() ? null : historialEstados.get(0);
    }

    public Pago registrarPago(BigDecimal monto, MedioPago medioPago, ConceptoPago concepto,
                              LocalDateTime fechaHora, String nroComprobante, String observaciones) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a cobrar debe ser mayor a cero.");
        }
        if (monto.compareTo(getSaldoPendiente()) > 0) {
            throw new IllegalArgumentException("El monto a cobrar supera el saldo pendiente de $" + getSaldoPendiente());
        }

        LocalDateTime fh = fechaHora != null ? fechaHora : LocalDateTime.now();
        Pago pago = Pago.builder()
                .monto(monto)
                .medioPago(medioPago)
                .concepto(concepto)
                .fechaHora(fh)
                .nroComprobante(nroComprobante)
                .observaciones(observaciones)
                .viaje(this)
                .build();

        this.pagos.add(0, pago);

        if (pago.esSenia()) {
            this.registrarSenia(monto, fh);
        } else {
            this.registrarCobroSaldo(monto, fh);
        }

        return pago;
    }

    public Pago conocerPago() {
        return this.pagos.isEmpty() ? null : this.pagos.get(0);
    }

    public List<Pago> conocerPagos() {
        return this.pagos;
    }

    public void registrarSenia(BigDecimal monto, LocalDateTime fechaHora) {
        this.estado.registrarSenia(this, getCambioEstadoActual(), monto, fechaHora);
    }

    public void registrarCobroSaldo(BigDecimal monto, LocalDateTime fechaHora) {
        this.estado.marcarListo(this, getCambioEstadoActual(), monto, fechaHora);
    }

    public void iniciarViaje(LocalDateTime fechaHora) {
        this.estado.iniciar(this, getCambioEstadoActual(), fechaHora);
    }

    public void interrumpirViaje(LocalDateTime fechaHora) {
        this.estado.interrumpir(this, getCambioEstadoActual(), fechaHora);
    }

    public void continuarViaje(Vehiculo nuevoVehiculo, TipoUnidad tipoAuxilio, LocalDateTime fechaHora) {
        this.estado.continuar(this, getCambioEstadoActual(), nuevoVehiculo, tipoAuxilio, fechaHora);
    }

    public void finalizarViaje(LocalDateTime fechaHora) {
        this.estado.finalizar(this, getCambioEstadoActual(), fechaHora);
    }

    public void cancelarViaje(LocalDateTime fechaHora) {
        this.estado.cancelar(this, getCambioEstadoActual(), fechaHora);
    }

    public void asignarNuevoColectivo(Vehiculo v, TipoUnidad tipoAuxilio) {
        if (v == null) return;
        boolean yaAsignado = unidades != null && unidades.stream()
                .anyMatch(unidad -> unidad.getVehiculo() != null
                        && Objects.equals(unidad.getVehiculo().getId(), v.getId()));

        if (!yaAsignado) {
            UnidadViaje nuevaUnidad = UnidadViaje.builder()
                    .tipo(tipoAuxilio)
                    .vehiculo(v)
                    .build();
            addUnidad(nuevaUnidad);
        }
    }

    public void addUnidad(UnidadViaje u) {
        unidades.add(u);
        u.setViaje(this);
    }

    public BigDecimal getSeniaSugerida() {
        if (this.montoTotal == null) return BigDecimal.ZERO;
        return this.montoTotal.multiply(new BigDecimal("0.30"));
    }

    public BigDecimal getTotalCobrado() {
        BigDecimal total = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(this.seniaPagada) && this.montoSenia != null) total = total.add(this.montoSenia);
        if (this.montoSaldo != null) total = total.add(this.montoSaldo);
        return total;
    }

    public BigDecimal getSaldoPendiente() {
        if (this.montoTotal == null) return BigDecimal.ZERO;
        BigDecimal saldo = this.montoTotal.subtract(getTotalCobrado());
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    public boolean estaTotalmentePagado() {
        return Boolean.TRUE.equals(this.totalPagado) && getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean tieneColectivosYChoferesAsignados() {
        return unidades != null && !unidades.isEmpty() && unidades.stream().allMatch(unidad ->
                (unidad.getVehiculo() != null || tieneTexto(unidad.getEmpresaTercera()))
                        && unidad.getChoferes() != null
                        && !unidad.getChoferes().isEmpty()
                        && unidad.getChoferes().stream().allMatch(asignacion ->
                        asignacion.getChofer() != null || tieneTexto(asignacion.getNombreTercero()))
        );
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}