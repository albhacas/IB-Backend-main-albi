package com.empresax.gestionflota.vehiculo.model;

import com.empresax.gestionflota.documentacion.model.DetalleDocumentacion;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Vehiculo")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @PositiveOrZero(message = "El número interno debe ser cero o positivo")
    private Integer interno;

    @NotNull(message = "El kilometraje es obligatorio")
    @PositiveOrZero(message = "El kilometraje debe ser cero o positivo")
    @Builder.Default
    private Integer kilometraje = 0;

    @Column(nullable = false, unique = true, length = 20)
    @Pattern(regexp = "^(?:[A-Za-z]{2} ?[0-9]{3} ?[A-Za-z]{2}|[A-Za-z]{3} ?[0-9]{3})$",
            message = "Patente inválida, debe ser de la forma AB 123 CD o ABC 123")
    private String patente;

    @Column(length = 4)
    @Pattern(regexp = "^(?:[0-9]{4})?$", message = "La chapa provincial debe contener exactamente 4 dígitos")
    private String chapaProvincial;

    @Column(name = "anio_fabricacion")
    private Integer anio;

    @PositiveOrZero(message = "La capacidad de pasajeros debe ser cero o positiva")
    private Integer capacidadPasajeros;

    @Column(length = 50)
    private String numeroChasis;

    @Column(length = 50)
    private String marcaChasis;

    @Column(length = 50)
    private String procedencia;

    @Column(length = 50)
    private String codigoAutomotor;

    @Column(length = 50)
    private String tipoVehiculo;

    @Column(length = 50)
    private String marcaMotor;

    @Column(length = 50)
    private String carroceria;

    @PositiveOrZero(message = "El peso debe ser cero o positivo")
    private Double peso;

    @Column(length = 50)
    @NotBlank(message = "El número de motor es obligatorio")
    @Pattern(regexp = "^[0-9]+$", message = "El número de motor debe contener solo dígitos")
    private String nroMotor;

    private Integer modeloAnio;

    private LocalDate fechaAdquisicion;
    private LocalDate fechaInscripcion;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(length = 30)
    @Builder.Default
    private String color = "#2563eb";

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(length = 50)
    @Builder.Default
    private String estado = "DISPONIBLE"; // "DISPONIBLE", "EN_MANTENIMIENTO", "FUERA_DE_SERVICIO"

    // NUEVO: Registro de motivo y fecha de baja
    @Column(length = 255)
    private String motivoBaja;

    private LocalDate fechaBaja;

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("vehiculo-documentacion")
    @Builder.Default
    private List<DetalleDocumentacion> documentaciones = new ArrayList<>();

    public void addDocumentacion(DetalleDocumentacion documentacion) {
        if (documentacion == null) {
            throw new IllegalArgumentException("La documentación es obligatoria.");
        }
        documentaciones.add(documentacion);
        documentacion.asignarVehiculo(this);
    }

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("vehiculo-matafuegos")
    @Builder.Default
    private List<Matafuego> matafuegos = new ArrayList<>();

    public void addMatafuego(Matafuego matafuego) {
        if (matafuego == null) {
            throw new IllegalArgumentException("El matafuego no puede ser nulo.");
        }
        matafuegos.add(matafuego);
        matafuego.setVehiculo(this);
    }
}