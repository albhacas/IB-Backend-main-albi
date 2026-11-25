package com.empresax.gestionflota.viaje.controller;

import com.empresax.gestionflota.viaje.dto.PagoAltaDTO;
import com.empresax.gestionflota.viaje.dto.PagoResponseDTO;
import com.empresax.gestionflota.viaje.model.ConceptoPago;
import com.empresax.gestionflota.viaje.model.MedioPago;
import com.empresax.gestionflota.viaje.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // REGISTRAR COBRO DE UN VIAJE
    @PostMapping("/viajes/{viajeId}/pagos")
    public ResponseEntity<?> registrarPago(@PathVariable Long viajeId, @RequestBody PagoAltaDTO dto) {
        try {
            PagoResponseDTO response = pagoService.registrarPago(viajeId, dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al registrar el cobro: " + e.getMessage()));
        }
    }

    // CONSULTAR COBROS DE UN VIAJE ESPECÍFICO
    @GetMapping("/viajes/{viajeId}/pagos")
    public ResponseEntity<List<PagoResponseDTO>> consultarPagosPorViaje(@PathVariable Long viajeId) {
        return ResponseEntity.ok(pagoService.consultarPagosPorViaje(viajeId));
    }

    // CONSULTAR TODOS LOS COBROS (CAJA GENERAL)
    @GetMapping("/pagos")
    public ResponseEntity<List<PagoResponseDTO>> consultarTodosLosPagos() {
        return ResponseEntity.ok(pagoService.consultarTodosLosPagos());
    }

    // LISTAR MEDIOS DE PAGO DISPONIBLES
    @GetMapping("/pagos/medios")
    public ResponseEntity<List<MedioPago>> listarMediosPago() {
        return ResponseEntity.ok(pagoService.listarMediosPago());
    }

    // LISTAR CONCEPTOS DE PAGO DISPONIBLES
    @GetMapping("/pagos/conceptos")
    public ResponseEntity<List<ConceptoPago>> listarConceptosPago() {
        return ResponseEntity.ok(pagoService.listarConceptosPago());
    }
}