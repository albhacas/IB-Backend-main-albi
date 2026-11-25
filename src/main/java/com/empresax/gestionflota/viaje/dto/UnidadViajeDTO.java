package com.empresax.gestionflota.viaje.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UnidadViajeDTO {
    private String nombreTipoUnidad;
    private Long vehiculoId;
    private String empresaTercera;
    private Integer cantidadColectivosTercero;
    private String patenteTercero;
    private List<AsignacionChoferDTO> choferes;
}