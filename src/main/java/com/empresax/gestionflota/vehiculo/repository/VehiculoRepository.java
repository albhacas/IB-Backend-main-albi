package com.empresax.gestionflota.vehiculo.repository;

import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    Optional<Vehiculo> findByPatente(String patente);
    boolean existsByPatente(String patente);
    boolean existsByPatenteAndIdNot(String patente, Long id);
    List<Vehiculo> findByActivoTrue();
}