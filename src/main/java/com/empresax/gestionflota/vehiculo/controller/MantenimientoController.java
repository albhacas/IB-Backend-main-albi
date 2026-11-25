package com.empresax.gestionflota.vehiculo.controller;

import com.empresax.gestionflota.vehiculo.dto.MantenimientoAltaDTO;
import com.empresax.gestionflota.vehiculo.model.Mantenimiento;
import com.empresax.gestionflota.vehiculo.service.MantenimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {
    private final MantenimientoService service;

    public MantenimientoController(MantenimientoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Mantenimiento>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<Mantenimiento>> historialPorVehiculo(@PathVariable Long vehiculoId) {
        return ResponseEntity.ok(service.obtenerHistorialPorVehiculo(vehiculoId));
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody MantenimientoAltaDTO dto) {
        try {
            Mantenimiento nuevo = service.registrarMantenimiento(dto);
            return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al registrar mantenimiento: " + e.getMessage()));
        }
    }
}