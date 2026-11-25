package com.empresax.gestionflota.chofer.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChoferAltaDTO {
    // Filiación
    private String nombre;
    private String apellido;
    private String dni;
    private String cuil;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private String domicilio;
    private String localidad;
    private String provincia;
    private String fotoUrl;

    // Habilitación
    private String ambitoHabilitacion;

    // Carnet Provincial
    private String nroCarnetProvincial;
    private String categoriaProvincial;
    private LocalDate vencimientoProvincial;

    // Carnet Nacional
    private String nroCarnetNacional;
    private String categoriaNacional;
    private LocalDate vencimientoNacional;

    // Certificado de Delitos Sexuales (Semestral - 6 meses)
    private LocalDate fechaEmisionDelitos;
    private LocalDate fechaVencimientoDelitos;
    private String estadoDelitos;
    private String archivoDelitosUrl;

    // Operativa
    private String turnoDisponibilidad;
    private String estadoInicial;
}