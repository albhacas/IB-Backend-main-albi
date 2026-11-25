package com.empresax.gestionflota.documentacion.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VIGENTE")
public class EsVigente extends EstadoDocumentacion {

    public EsVigente() {
        super("Vigente", "La documentación está vigente.");
    }

    @Override
    public EstadoDocumentacion crearProximoEstado() {
        return new EsPorVencer();
    }
}
