package com.empresax.gestionflota.documentacion.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentoDigitalDTO {
    private String categoria;
    private Long entidadId;
    private String tipo;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String archivoNombre;
    private String formato;
}