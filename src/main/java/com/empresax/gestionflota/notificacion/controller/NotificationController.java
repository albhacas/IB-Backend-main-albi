package com.empresax.gestionflota.notificacion.controller;

import com.empresax.gestionflota.notificacion.dto.PushSubscriptionDTO;
import com.empresax.gestionflota.notificacion.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> obtenerClavePublica() {
        return ResponseEntity.ok(Map.of("publicKey", service.getPublicKey()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> suscribir(@RequestBody PushSubscriptionDTO dto) {
        try {
            service.guardarSuscripcion(dto);
            return ResponseEntity.ok(Map.of("mensaje", "Suscripción registrada con éxito."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint de prueba para enviar una notificación inmediata
    @PostMapping("/test")
    public ResponseEntity<?> enviarTest(@RequestBody Map<String, String> body) {
        String titulo = body.getOrDefault("titulo", "IterBus Central");
        String mensaje = body.getOrDefault("mensaje", "Notificación del sistema operativo");
        service.enviarNotificacionATodos(titulo, mensaje, "/");
        return ResponseEntity.ok(Map.of("mensaje", "Notificación enviada"));
    }
}