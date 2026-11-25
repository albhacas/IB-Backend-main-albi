package com.empresax.gestionflota.documentacion.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("NO_VIGENTE")
public class EsNoVigente extends EstadoDocumentacion {

    public EsNoVigente() {
        super("No vigente", "La documentación está vencida.");
    }

    @Override
    public EstadoDocumentacion crearProximoEstado() {
        return new EsVigente();
    }
}
