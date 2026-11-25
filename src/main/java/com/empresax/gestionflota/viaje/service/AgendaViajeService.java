package com.empresax.gestionflota.viaje.service;

import com.empresax.gestionflota.viaje.model.AgendaViaje;
import com.empresax.gestionflota.viaje.model.Viaje;
import com.empresax.gestionflota.viaje.repository.AgendaViajeRepository;
import com.empresax.gestionflota.viaje.repository.ViajeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AgendaViajeService {

	private final AgendaViajeRepository agendaViajeRepository;
	private final ViajeRepository viajeRepository;

	public AgendaViajeService(AgendaViajeRepository agendaViajeRepository, ViajeRepository viajeRepository) {
		this.agendaViajeRepository = agendaViajeRepository;
		this.viajeRepository = viajeRepository;
	}

	@Transactional
	public AgendaViaje crear(AgendaViaje agendaViaje) {
		if (agendaViaje.getAnio() == null || agendaViaje.getAnio() < 1) {
			throw new IllegalArgumentException("El año debe ser válido");
		}
		if (agendaViaje.getMes() == null || agendaViaje.getMes() < 1 || agendaViaje.getMes() > 12) {
			throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
		}
		if (agendaViajeRepository.existsByAnioAndMes(agendaViaje.getAnio(), agendaViaje.getMes())) {
			throw new IllegalArgumentException("Ya existe una agenda para ese año y mes");
		}

		agendaViaje.setFechaCreacion(LocalDate.now());
		return agendaViajeRepository.save(agendaViaje);
	}

	@Transactional(readOnly = true)
	public List<AgendaViaje> listarPorAnio(Integer anio) {
		return agendaViajeRepository.findByAnio(anio);
	}

	@Transactional(readOnly = true)
	public AgendaViaje buscarPorId(Long agendaId) {
		return agendaViajeRepository.findById(agendaId)
				.orElseThrow(() -> new EntityNotFoundException("No existe la agenda " + agendaId));
	}

	@Transactional
	public AgendaViaje agregarViaje(Long agendaId, Long viajeId) {
		AgendaViaje agendaViaje = buscarPorId(agendaId);
		Viaje viaje = viajeRepository.findById(viajeId)
				.orElseThrow(() -> new EntityNotFoundException("No existe el viaje " + viajeId));

		agendaViaje.agregarViaje(viaje);
		return agendaViajeRepository.save(agendaViaje);
	}

	@Transactional
	public AgendaViaje quitarViaje(Long agendaId, Long viajeId) {
		AgendaViaje agendaViaje = buscarPorId(agendaId);
		Viaje viaje = agendaViaje.consultarViaje(viajeId)
				.orElseThrow(() -> new EntityNotFoundException(
						"El viaje " + viajeId + " no pertenece a la agenda " + agendaId));

		agendaViaje.quitarViaje(viaje);
		return agendaViajeRepository.save(agendaViaje);
	}

	@Transactional(readOnly = true)
	public Optional<Viaje> consultarViaje(Long agendaId, Long viajeId) {
		AgendaViaje agendaViaje = buscarPorId(agendaId);
		return agendaViaje.consultarViaje(viajeId);
	}
}
