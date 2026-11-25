package com.empresax.gestionflota.usuario.controller;

import com.empresax.gestionflota.usuario.dto.UsuarioActualizacionDTO;
import com.empresax.gestionflota.usuario.dto.UsuarioRegistroDTO;
import com.empresax.gestionflota.usuario.dto.UsuarioResponseDTO;
import com.empresax.gestionflota.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody UsuarioRegistroDTO dto) {
        try {
            UsuarioResponseDTO creado = service.registrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody UsuarioActualizacionDTO dto) {
        try {
            return ResponseEntity.ok(service.actualizar(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
