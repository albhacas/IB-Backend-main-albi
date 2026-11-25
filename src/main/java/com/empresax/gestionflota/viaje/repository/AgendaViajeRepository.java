package com.empresax.gestionflota.viaje.repository;

import com.empresax.gestionflota.viaje.model.AgendaViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgendaViajeRepository extends JpaRepository<AgendaViaje, Long> {

	Optional<AgendaViaje> findByAnioAndMes(Integer anio, Integer mes);

	List<AgendaViaje> findByAnio(Integer anio);

	boolean existsByAnioAndMes(Integer anio, Integer mes);
}
