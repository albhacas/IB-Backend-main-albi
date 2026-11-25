package com.empresax.gestionflota.chofer.controller;

import com.empresax.gestionflota.chofer.dto.ChoferAltaDTO;
import com.empresax.gestionflota.chofer.model.Chofer;
import com.empresax.gestionflota.chofer.service.ChoferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/choferes")
public class ChoferController {

    private final ChoferService service;

    public ChoferController(ChoferService service) {
        this.service = service;
    }

    // IG1-23: Consultar Choferes
    @GetMapping
    public ResponseEntity<List<Chofer>> listar(@RequestParam(required = false, defaultValue = "false") Boolean soloActivos) {
        return ResponseEntity.ok(service.listarTodos(soloActivos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chofer> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // IG1-21: Registrar Chofer
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody ChoferAltaDTO dto) {
        try {
            Chofer chofer = service.registrarDesdeDTO(dto);
            return new ResponseEntity<>(chofer, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarConDocumentos(
            @RequestPart("datos") ChoferAltaDTO dto,
            @RequestPart(value = "carnetProvincial", required = false) MultipartFile carnetProvincial,
            @RequestPart(value = "carnetNacional", required = false) MultipartFile carnetNacional,
            @RequestPart(value = "certificadoDelitos", required = false) MultipartFile certificadoDelitos) {
        try {
            Chofer chofer = service.registrarDesdeDTO(
                    dto, carnetProvincial, carnetNacional, certificadoDelitos);
            return new ResponseEntity<>(chofer, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al registrar el chofer: " + e.getMessage()));
        }
    }

    // IG1-22: Modificar Chofer
    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable Long id, @RequestBody ChoferAltaDTO dto) {
        try {
            Chofer actualizado = service.actualizarDesdeDTO(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> modificarConDocumentos(
            @PathVariable Long id,
            @RequestPart("datos") ChoferAltaDTO dto,
            @RequestPart(value = "carnetProvincial", required = false) MultipartFile carnetProvincial,
            @RequestPart(value = "carnetNacional", required = false) MultipartFile carnetNacional,
            @RequestPart(value = "certificadoDelitos", required = false) MultipartFile certificadoDelitos) {
        try {
            Chofer actualizado = service.actualizarDesdeDTO(
                    id, dto, carnetProvincial, carnetNacional, certificadoDelitos);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // IG1-24: Registrar baja de Chofer
    @PatchMapping("/{id}/baja")
    public ResponseEntity<?> darDeBaja(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        try {
            String motivo = payload != null ? payload.get("motivo") : "Baja operativa regular";
            Chofer chofer = service.darDeBaja(id, motivo);
            return ResponseEntity.ok(chofer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // IG1-25: Reactivar / Dar de alta a Chofer
    @PatchMapping("/{id}/alta")
    public ResponseEntity<?> darDeAlta(@PathVariable Long id) {
        try {
            Chofer chofer = service.darDeAlta(id);
            return ResponseEntity.ok(chofer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}