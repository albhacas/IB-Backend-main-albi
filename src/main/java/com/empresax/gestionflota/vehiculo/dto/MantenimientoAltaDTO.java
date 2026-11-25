package com.empresax.gestionflota.vehiculo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MantenimientoAltaDTO {
    @NotNull(message = "El vehículo es obligatorio")
    private Long vehiculoId;

    @NotBlank(message = "El tipo de servicio es obligatorio")
    private String tipoServicio;

    @NotNull(message = "La fecha de mantenimiento es obligatoria")
    private LocalDate fechaMantenimiento;

    private LocalDate fechaEstimadaEntrega;

    @PositiveOrZero(message = "El costo debe ser cero o un valor positivo")
    private BigDecimal costo;

    private String observaciones;
}