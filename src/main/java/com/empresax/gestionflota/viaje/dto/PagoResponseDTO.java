package com.empresax.gestionflota.viaje.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponseDTO {
    private Long id;
    private Long viajeId;
    private LocalDateTime fechaHora;
    private BigDecimal monto;
    private String medioPago;
    private String concepto;
    private String nroComprobante;
    private String observaciones;
    private BigDecimal saldoRestanteViaje;
    private String estadoViajeResultante;
}