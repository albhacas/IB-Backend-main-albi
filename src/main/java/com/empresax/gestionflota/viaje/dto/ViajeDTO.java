package com.empresax.gestionflota.viaje.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ViajeDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String origen;
    private String destino;
    private String nombreTipoViaje;
    private BigDecimal montoTotal;
    private BigDecimal montoSenia;
    private List<UnidadViajeDTO> unidades;
    private String paradas;
    private String instruccionesRuta;
    private Boolean esIdaYVuelta;
    private LocalDate fechaLlegadaIda;
    private LocalTime horaLlegadaIda;
    private LocalDate fechaRegreso;
    private LocalTime horaRegreso;
    private String itinerarioJson;
    private Long clienteId;
}