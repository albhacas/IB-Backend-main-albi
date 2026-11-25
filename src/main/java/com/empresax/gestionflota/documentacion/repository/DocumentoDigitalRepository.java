package com.empresax.gestionflota.documentacion.repository;

import com.empresax.gestionflota.documentacion.model.DocumentoDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoDigitalRepository extends JpaRepository<DocumentoDigital, Long> {
    List<DocumentoDigital> findByCategoriaOrderByIdDesc(String categoria);
    List<DocumentoDigital> findByCategoriaAndEntidadIdOrderByIdDesc(String categoria, Long entidadId);
    Optional<DocumentoDigital> findFirstByCategoriaAndEntidadIdAndTipoIgnoreCaseOrderByIdDesc(
            String categoria, Long entidadId, String tipo);
}