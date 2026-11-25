package com.empresax.gestionflota.viaje.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medio_pago_id", nullable = false)
    private MedioPago medioPago;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "concepto_pago_id", nullable = false)
    private ConceptoPago concepto;

    @Column(length = 100)
    private String nroComprobante;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    @JsonBackReference
    private Viaje viaje;

    // Métodos del Diagrama de Clases
    public boolean esSenia() {
        return this.concepto != null && 
               ("SEÑA".equalsIgnoreCase(this.concepto.getNombre().trim()) || 
                "SENIA".equalsIgnoreCase(this.concepto.getNombre().trim()));
    }

    public boolean esSaldo() {
        return !esSenia();
    }

    public MedioPago conocerMedioPago() {
        return this.medioPago;
    }

    public ConceptoPago conocerConceptoPago() {
        return this.concepto;
    }
}