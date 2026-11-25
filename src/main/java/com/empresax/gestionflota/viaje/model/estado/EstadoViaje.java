package com.empresax.gestionflota.viaje.model.estado;

import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.viaje.model.CambioEstado;
import com.empresax.gestionflota.viaje.model.TipoUnidad;
import com.empresax.gestionflota.viaje.model.Viaje;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "EstadoViaje")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_estado", discriminatorType = DiscriminatorType.STRING)
@Getter @Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Programado.class, name = "PROGRAMADO"),
    @JsonSubTypes.Type(value = Confirmado.class, name = "CONFIRMADO"),
    @JsonSubTypes.Type(value = ListoParaSalir.class, name = "LISTO_PARA_SALIR"),
    @JsonSubTypes.Type(value = EnCurso.class, name = "EN_CURSO"),
    @JsonSubTypes.Type(value = Interrumpido.class, name = "INTERRUMPIDO"),
    @JsonSubTypes.Type(value = Finalizado.class, name = "FINALIZADO"),
    @JsonSubTypes.Type(value = Cancelado.class, name = "CANCELADO")
})
public abstract class EstadoViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    public EstadoViaje() {}

    public EstadoViaje(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public CambioEstado crearCambioEstado(LocalDateTime horaInicio, EstadoViaje e) {
        return CambioEstado.crear(horaInicio, e);
    }

    // Métodos polimórficos de la máquina de estados
    public void registrarSenia(Viaje viaje, CambioEstado actual, BigDecimal monto, LocalDateTime fechaHora) {
        throw new IllegalStateException("No se puede registrar seña en estado " + this.nombre);
    }

    public void marcarListo(Viaje viaje, CambioEstado actual, BigDecimal monto, LocalDateTime fechaHora) {
        throw new IllegalStateException("No se puede marcar listo para salir en estado " + this.nombre);
    }

    public void iniciar(Viaje viaje, CambioEstado actual, LocalDateTime fechaHora) {
        throw new IllegalStateException("No se puede iniciar el viaje en estado " + this.nombre);
    }

    public void interrumpir(Viaje viaje, CambioEstado actual, LocalDateTime fechaHora) {
        throw new IllegalStateException("No se puede interrumpir el viaje en estado " + this.nombre);
    }

    public void continuar(Viaje viaje, CambioEstado actual, Vehiculo nuevoVehiculo, TipoUnidad tipoAuxilio, LocalDateTime fechaHora) {
        throw new IllegalStateException("No se puede continuar el viaje en estado " + this.nombre);
    }

    public void finalizar(Viaje viaje, CambioEstado actual, LocalDateTime fechaHora) {
        throw new IllegalStateException("No se puede finalizar el viaje en estado " + this.nombre);
    }

    public void cancelar(Viaje viaje, CambioEstado actual, LocalDateTime fechaHora) {
        throw new IllegalStateException("No se puede cancelar el viaje en estado " + this.nombre);
    }

    public abstract EstadoViaje crearProximoEstado();
}