package com.empresax.gestionflota.viaje.repository;

import com.empresax.gestionflota.viaje.model.TipoViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoViajeRepository extends JpaRepository<TipoViaje, Long> {
    Optional<TipoViaje> findByNombre(String nombre);
}