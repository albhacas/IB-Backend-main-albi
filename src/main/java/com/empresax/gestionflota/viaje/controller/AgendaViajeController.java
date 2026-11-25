package com.empresax.gestionflota.viaje.controller;

import com.empresax.gestionflota.viaje.model.AgendaViaje;
import com.empresax.gestionflota.viaje.model.Viaje;
import com.empresax.gestionflota.viaje.service.AgendaViajeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendas-viaje")
public class AgendaViajeController {

	private final AgendaViajeService agendaViajeService;

	public AgendaViajeController(AgendaViajeService agendaViajeService) {
		this.agendaViajeService = agendaViajeService;
	}

	@PostMapping
	public ResponseEntity<AgendaResponse> crear(@RequestBody CrearAgendaRequest request) {
		AgendaViaje agenda = new AgendaViaje();
		agenda.setAnio(request.anio());
		agenda.setMes(request.mes());
		agenda.setObservaciones(request.observaciones());

		AgendaViaje creada = agendaViajeService.crear(agenda);
		return ResponseEntity.status(HttpStatus.CREATED).body(toAgendaResponse(creada));
	}

	@GetMapping
	public ResponseEntity<List<AgendaResponse>> listarPorAnio(@RequestParam Integer anio) {
		List<AgendaResponse> agendas = agendaViajeService.listarPorAnio(anio).stream()
				.map(this::toAgendaResponse)
				.toList();
		return ResponseEntity.ok(agendas);
	}

	@GetMapping("/{agendaId}")
	public ResponseEntity<AgendaResponse> buscarPorId(@PathVariable Long agendaId) {
		return ResponseEntity.ok(toAgendaResponse(agendaViajeService.buscarPorId(agendaId)));
	}

	@PostMapping("/{agendaId}/viajes/{viajeId}")
	public ResponseEntity<Void> agregarViaje(@PathVariable Long agendaId, @PathVariable Long viajeId) {
		agendaViajeService.agregarViaje(agendaId, viajeId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{agendaId}/viajes/{viajeId}")
	public ResponseEntity<Void> quitarViaje(@PathVariable Long agendaId, @PathVariable Long viajeId) {
		agendaViajeService.quitarViaje(agendaId, viajeId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{agendaId}/viajes/{viajeId}")
	public ResponseEntity<ViajeResponse> consultarViaje(@PathVariable Long agendaId,
														 @PathVariable Long viajeId) {
		Viaje viaje = agendaViajeService.consultarViaje(agendaId, viajeId)
				.orElseThrow(() -> new EntityNotFoundException(
						"El viaje " + viajeId + " no pertenece a la agenda " + agendaId));
		return ResponseEntity.ok(toViajeResponse(viaje));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(IllegalArgumentException exception) {
		return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Map<String, String>> manejarNoEncontrado(EntityNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
	}

	private AgendaResponse toAgendaResponse(AgendaViaje agenda) {
		return new AgendaResponse(agenda.getId(), agenda.getAnio(), agenda.getMes(),
				agenda.getFechaCreacion(), agenda.getObservaciones());
	}

	private ViajeResponse toViajeResponse(Viaje viaje) {
		return new ViajeResponse(viaje.getId(), viaje.getFechaInicio(), viaje.getFechaFin(),
				viaje.getHoraInicio(), viaje.getHoraFin(), viaje.getOrigen(), viaje.getDestino(),
				viaje.getMontoTotal(), viaje.getMontoSenia(), viaje.getMontoSaldo());
	}

	public record CrearAgendaRequest(Integer anio, Integer mes, String observaciones) {}

	public record AgendaResponse(Long id, Integer anio, Integer mes,
								 LocalDate fechaCreacion, String observaciones) {}

	public record ViajeResponse(Long id, LocalDate fechaInicio, LocalDate fechaFin,
								LocalTime horaInicio, LocalTime horaFin, String origen,
								String destino, BigDecimal montoTotal,
								BigDecimal montoSenia, BigDecimal montoSaldo) {}
}
