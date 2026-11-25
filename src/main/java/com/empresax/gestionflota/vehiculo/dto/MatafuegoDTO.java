package com.empresax.gestionflota.vehiculo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MatafuegoDTO {
    private Long id;
    private String ubicacion;
    private String numeroSerie;
    private LocalDate fechaCarga;

    @NotNull(message = "La fecha de vencimiento del matafuego es obligatoria")
    private LocalDate fechaVencimiento;

    private String observaciones;
}