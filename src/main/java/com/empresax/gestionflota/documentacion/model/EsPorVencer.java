package com.empresax.gestionflota.documentacion.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("POR_VENCER")
public class EsPorVencer extends EstadoDocumentacion {

    public EsPorVencer() {
        super("Por vencer", "La documentación vence dentro de los próximos 30 días.");
    }

    @Override
    public EstadoDocumentacion crearProximoEstado() {
        return new EsNoVigente();
    }
}
