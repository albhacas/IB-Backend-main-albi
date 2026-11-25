package com.empresax.gestionflota.viaje.repository;

import com.empresax.gestionflota.viaje.model.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {
    List<Viaje> findByActivoTrue();
    boolean existsByCliente_Id(Long clienteId);
}
