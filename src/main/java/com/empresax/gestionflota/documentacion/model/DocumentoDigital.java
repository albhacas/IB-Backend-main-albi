package com.empresax.gestionflota.documentacion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "DocumentoDigital")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentoDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String categoria; // "vehiculos", "clientes", "viajes", "choferes"

    @Column(nullable = false)
    private Long entidadId;

    @Column(nullable = false, length = 120)
    private String tipo; // Ej: "RTO provincial", "Póliza", "Recibo seguro", etc.

    @Column(nullable = false)
    private LocalDate fechaEmision;

    private LocalDate fechaVencimiento;

    @Column(length = 50)
    private String estado;

    @Column(length = 100)
    private String nro;

    @Column(length = 255)
    private String archivoNombre;

    @Column(length = 120)
    private String tipoContenido;

    private Long tamanioBytes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean tieneArchivo = false;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Column(length = 10)
    @Builder.Default
    private String formato = "PDF";

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}