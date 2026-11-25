package com.empresax.gestionflota.viaje.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModificarAgendaDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String paradas;             // Lista separada por comas o punto y coma
    private List<String> paradasLista;  // Soporte si el frontend envía arreglo directo
    private String instruccionesRuta;   // Instrucciones especiales de la hoja de ruta
    private Boolean esIdaYVuelta;
    private LocalDate fechaLlegadaIda;
    private LocalTime horaLlegadaIda;
    private LocalDate fechaRegreso;
    private LocalTime horaRegreso;
    private String itinerarioJson;
}