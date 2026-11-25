package com.empresax.gestionflota.viaje.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TransicionEstadoDTO {
    private BigDecimal monto;
    private Long nuevoVehiculoId;
}