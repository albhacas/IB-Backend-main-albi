package com.empresax.gestionflota.viaje.repository;

import com.empresax.gestionflota.viaje.model.MedioPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedioPagoRepository extends JpaRepository<MedioPago, Long> {
    Optional<MedioPago> findByNombreIgnoreCase(String nombre);
}