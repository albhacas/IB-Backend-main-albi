package com.empresax.gestionflota.cliente.controller;

import com.empresax.gestionflota.cliente.dto.RepresentanteAltaDTO;
import com.empresax.gestionflota.cliente.model.Representante;
import com.empresax.gestionflota.cliente.service.RepresentanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/representantes")
public class RepresentanteController {

    private final RepresentanteService service;

    public RepresentanteController(RepresentanteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Representante>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Representante> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody RepresentanteAltaDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable Long id,
                                       @Valid @RequestBody RepresentanteAltaDTO dto) {
        try {
            return ResponseEntity.ok(service.actualizar(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
