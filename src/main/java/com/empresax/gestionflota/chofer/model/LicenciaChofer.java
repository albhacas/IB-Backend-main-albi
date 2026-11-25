package com.empresax.gestionflota.chofer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "LicenciaChofer")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LicenciaChofer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chofer_id", nullable = false)
    @JsonBackReference
    private Chofer chofer;

    @Column(nullable = false, length = 50)
    private String tipoLicencia; // "PROVINCIAL" o "NACIONAL"

    @Column(nullable = false, length = 50)
    private String numeroLicencia;

    @Column(length = 100)
    private String categoria;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(length = 500)
    private String fotoFrente;

    @Column(length = 500)
    private String fotoDorso;
}