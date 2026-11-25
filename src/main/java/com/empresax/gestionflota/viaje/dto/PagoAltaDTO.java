package com.empresax.gestionflota.viaje.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoAltaDTO {
    private BigDecimal monto;
    private String nombreMedioPago;     // "Efectivo", "Transferencia", etc.
    private String nombreConcepto;      // "Seña", "Saldo"
    private String nroComprobante;
    private String observaciones;
    private LocalDateTime fechaHora;
}