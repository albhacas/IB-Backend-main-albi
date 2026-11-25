package com.empresax.gestionflota.usuario.controller;

import com.empresax.gestionflota.usuario.dto.LoginRequestDTO;
import com.empresax.gestionflota.usuario.dto.LoginResponseDTO;
import com.empresax.gestionflota.usuario.dto.RegistroInicialDTO;
import com.empresax.gestionflota.usuario.dto.UsuarioResponseDTO;
import com.empresax.gestionflota.usuario.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/registro-inicial/disponible")
    public ResponseEntity<Map<String, Boolean>> registroInicialDisponible() {
        return ResponseEntity.ok(Map.of("disponible", authService.registroInicialDisponible()));
    }

    @PostMapping("/registro-inicial")
    public ResponseEntity<?> registrarInicial(@Valid @RequestBody RegistroInicialDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarInicial(request));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> usuarioActual(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.usuarioActual(jwt.getSubject()));
    }
}
