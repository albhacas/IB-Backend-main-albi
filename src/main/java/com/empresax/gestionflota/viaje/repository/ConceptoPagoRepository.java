package com.empresax.gestionflota.viaje.repository;

import com.empresax.gestionflota.viaje.model.ConceptoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConceptoPagoRepository extends JpaRepository<ConceptoPago, Long> {
    Optional<ConceptoPago> findByNombreIgnoreCase(String nombre);
}