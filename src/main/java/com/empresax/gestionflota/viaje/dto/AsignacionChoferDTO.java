package com.empresax.gestionflota.viaje.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AsignacionChoferDTO {
    private String rol;
    private LocalDate fecha;
    private Long choferId;
    private String nombreTercero;
    private String dniTercero;
    private String telefonoTercero;
}