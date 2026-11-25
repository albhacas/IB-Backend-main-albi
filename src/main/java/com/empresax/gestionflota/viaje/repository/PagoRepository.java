package com.empresax.gestionflota.viaje.repository;

import com.empresax.gestionflota.viaje.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByViajeIdOrderByFechaHoraDesc(Long viajeId);
    List<Pago> findAllByOrderByFechaHoraDesc();
}