package com.empresax.gestionflota.vehiculo.repository;

import com.empresax.gestionflota.vehiculo.model.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    List<Mantenimiento> findByVehiculoId(Long vehiculoId);
}