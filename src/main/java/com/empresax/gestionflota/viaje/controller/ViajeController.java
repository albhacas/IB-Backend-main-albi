package com.empresax.gestionflota.viaje.controller;

import com.empresax.gestionflota.viaje.dto.ModificarAgendaDTO;
import com.empresax.gestionflota.viaje.dto.TransicionEstadoDTO;
import com.empresax.gestionflota.viaje.dto.UnidadViajeDTO;
import com.empresax.gestionflota.viaje.dto.ViajeDTO;
import com.empresax.gestionflota.viaje.model.Viaje;
import com.empresax.gestionflota.viaje.service.ViajeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {
    private final ViajeService viajeService;

    public ViajeController(ViajeService viajeService) {
        this.viajeService = viajeService;
    }

    @GetMapping
    public ResponseEntity<List<Viaje>> listar() {
        return ResponseEntity.ok(viajeService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(viajeService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody ViajeDTO dto) {
        try {
            Viaje guardado = viajeService.crearViaje(dto);
            return new ResponseEntity<>(guardado, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable Long id, @RequestBody ViajeDTO dto) {
        try {
            Viaje actualizado = viajeService.actualizarViaje(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- NUEVO: MODIFICAR AGENDA / HOJA DE RUTA / DRAG & DROP ---
    @RequestMapping(value = "/{id}/agenda", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> modificarAgenda(@PathVariable Long id, @RequestBody ModificarAgendaDTO dto) {
        try {
            Viaje actualizado = viajeService.modificarAgenda(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            viajeService.eliminarViaje(id);
            return ResponseEntity.ok(Map.of("mensaje", "Viaje dado de baja exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/unidades")
    public ResponseEntity<?> agregarUnidad(@PathVariable Long id, @RequestBody UnidadViajeDTO dto) {
        try {
            return ResponseEntity.ok(viajeService.agregarUnidad(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/senia")
    public ResponseEntity<?> registrarSenia(@PathVariable Long id, @RequestBody(required = false) TransicionEstadoDTO dto) {
        try {
            return ResponseEntity.ok(viajeService.registrarSenia(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cobro-saldo")
    public ResponseEntity<?> registrarCobroSaldo(@PathVariable Long id,
                                                 @RequestBody(required = false) TransicionEstadoDTO dto) {
        try {
            return ResponseEntity.ok(viajeService.registrarCobroSaldo(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<?> iniciarViaje(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(viajeService.iniciarViaje(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/interrumpir")
    public ResponseEntity<?> interrumpirViaje(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(viajeService.interrumpirViaje(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/continuar")
    public ResponseEntity<?> continuarViaje(@PathVariable Long id, @RequestBody(required = false) TransicionEstadoDTO dto) {
        try {
            return ResponseEntity.ok(viajeService.continuarViaje(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarViaje(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(viajeService.finalizarViaje(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarViaje(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(viajeService.cancelarViaje(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}