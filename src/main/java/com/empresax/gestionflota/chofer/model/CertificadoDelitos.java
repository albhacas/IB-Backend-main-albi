package com.empresax.gestionflota.chofer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "CertificadoDelitos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CertificadoDelitos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCertificado")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdChofer", nullable = false)
    @JsonBackReference
    private Chofer chofer;

    @Column(name = "ArchivoUrl", length = 500)
    private String archivoUrl;

    @Column(name = "FechaEmision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "FechaVencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "Estado", length = 50)
    private String estado; // "Vigente", "Vencido", "En Trámite"
}