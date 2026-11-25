package com.empresax.gestionflota.vehiculo.controller;

import com.empresax.gestionflota.vehiculo.dto.MatafuegoAlertaDTO;
import com.empresax.gestionflota.vehiculo.dto.MatafuegoDTO;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.vehiculo.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService service;

    public VehiculoController(VehiculoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Vehiculo>> listar(@RequestParam(required = false, defaultValue = "false") Boolean soloActivos) {
        return ResponseEntity.ok(service.listarTodos(soloActivos));
    }

    // 1. Consultar Vehículo por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody Vehiculo vehiculo) {
        try {
            Vehiculo guardado = service.registrar(vehiculo);
            return new ResponseEntity<>(guardado, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar vehículo: " + e.getMessage()));
        }
    }

    // 2. Modificar Vehículo
    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable Long id, @Valid @RequestBody Vehiculo vehiculo) {
        try {
            Vehiculo actualizado = service.actualizar(id, vehiculo);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar vehículo: " + e.getMessage()));
        }
    }

    // 3. Registrar Baja de Vehículo
    @PatchMapping("/{id}/baja")
    public ResponseEntity<?> darDeBaja(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        try {
            String motivo = payload != null ? payload.get("motivo") : "Baja operativa general";
            Vehiculo vehiculo = service.darDeBaja(id, motivo);
            return ResponseEntity.ok(vehiculo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Reactivar / Alta
    @PatchMapping("/{id}/alta")
    public ResponseEntity<?> darDeAlta(@PathVariable Long id) {
        try {
            Vehiculo vehiculo = service.darDeAlta(id);
            return ResponseEntity.ok(vehiculo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/matafuegos")
    public ResponseEntity<?> actualizarMatafuegos(@PathVariable Long id,
                                                  @Valid @RequestBody List<MatafuegoDTO> matafuegos) {
        try {
            return ResponseEntity.ok(service.actualizarMatafuegos(id, matafuegos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/matafuegos/alertas")
    public ResponseEntity<List<MatafuegoAlertaDTO>> alertasMatafuegos(
            @RequestParam(required = false, defaultValue = "10") Integer dias) {
        return ResponseEntity.ok(service.obtenerAlertasMatafuegos(dias));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Los datos del vehículo no son válidos.");
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }
}