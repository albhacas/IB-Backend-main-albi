package com.empresax.gestionflota.vehiculo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "mantenimiento")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Mantenimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    // Columna preexistente en la base de datos
    @Column(name = "patente_vehiculo")
    private String patenteVehiculo;

    @Column(nullable = false, length = 50)
    private String tipoServicio;

    @Column(nullable = false)
    private LocalDate fechaMantenimiento;

    @Column(name = "fecha_ingreso")
    @Builder.Default
    private LocalDate fechaIngreso = LocalDate.now();

    @Column
    private LocalDate fechaEstimadaEntrega;

    @Column(precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(length = 1000)
    private String observaciones;

    @PrePersist
    @PreUpdate
    public void alPersistir() {
        if (this.fechaIngreso == null) {
            this.fechaIngreso = (this.fechaMantenimiento != null) ? this.fechaMantenimiento : LocalDate.now();
        }
        if ((this.patenteVehiculo == null || this.patenteVehiculo.isBlank()) && this.vehiculo != null) {
            this.patenteVehiculo = this.vehiculo.getPatente();
        }
    }
}