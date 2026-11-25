package com.empresax.gestionflota.usuario.service;

import com.empresax.gestionflota.usuario.dto.LoginRequestDTO;
import com.empresax.gestionflota.usuario.dto.LoginResponseDTO;
import com.empresax.gestionflota.usuario.dto.RegistroInicialDTO;
import com.empresax.gestionflota.usuario.dto.UsuarioResponseDTO;
import com.empresax.gestionflota.usuario.model.Usuario;
import com.empresax.gestionflota.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long expirationMs;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioService usuarioService,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder,
                       @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expirationMs = expirationMs;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(expirationMs);
        return new LoginResponseDTO(
                crearToken(usuario, issuedAt, expiresAt), toResponse(usuario), expiresAt.toEpochMilli());
    }

    public UsuarioResponseDTO registrarInicial(RegistroInicialDTO request) {
        return usuarioService.registrarInicial(
                request.nombre(), request.apellido(), request.email(), request.password());
    }

    @Transactional(readOnly = true)
    public boolean registroInicialDisponible() {
        return usuarioService.registroInicialDisponible();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO usuarioActual(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "La cuenta ya no está disponible."));
    }

    private String crearToken(Usuario usuario, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("iterbus")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("role", usuario.getRol().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getEmail());
    }
}
