package com.empresax.gestionflota.vehiculo.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MatafuegoAlertaDTO {
    private Long matafuegoId;
    private Long vehiculoId;
    private Integer interno;
    private String patente;
    private String tipoVehiculo;
    private String ubicacion;
    private String numeroSerie;
    private LocalDate fechaVencimiento;
    private long diasRestantes;
    private String estado; // "VENCIDO" o "POR_VENCER"
}