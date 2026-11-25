package com.empresax.gestionflota.usuario.service;

import com.empresax.gestionflota.usuario.dto.LoginRequestDTO;
import com.empresax.gestionflota.usuario.dto.LoginResponseDTO;
import com.empresax.gestionflota.usuario.model.RolUsuario;
import com.empresax.gestionflota.usuario.model.Usuario;
import com.empresax.gestionflota.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                usuarioRepository, usuarioService, passwordEncoder, jwtEncoder, 300_000L);
    }

    @Test
    void loginNormalizaElCorreoYDevuelveTokenYPerfil() {
        Usuario usuario = Usuario.builder()
                .id(12L)
                .nombre("Ana")
                .apellido("Pérez")
                .email("ana@ejemplo.com")
                .rol(RolUsuario.GERENTE_ADMINISTRATIVO)
                .passwordHash("hash-seguro")
                .build();
        Jwt token = Jwt.withTokenValue("jwt-firmado")
                .header("alg", "HS256")
                .subject("ana@ejemplo.com")
                .build();
        when(usuarioRepository.findByEmailIgnoreCase("ana@ejemplo.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-segura", "hash-seguro")).thenReturn(true);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(token);

        long startTime = System.currentTimeMillis();
        LoginResponseDTO respuesta = authService.login(
                new LoginRequestDTO(" ANA@Ejemplo.com ", "clave-segura"));

        assertEquals("jwt-firmado", respuesta.token());
        assertEquals(12L, respuesta.usuario().id());
        assertEquals(RolUsuario.GERENTE_ADMINISTRATIVO, respuesta.usuario().rol());
        assertTrue(respuesta.expiresAt() >= startTime + 300_000L);
        assertTrue(respuesta.expiresAt() <= System.currentTimeMillis() + 300_000L);
        verify(passwordEncoder).matches("clave-segura", "hash-seguro");
    }

    @Test
    void loginRechazaCredencialesInvalidasSinEmitirToken() {
        Usuario usuario = Usuario.builder()
                .email("ana@ejemplo.com")
                .passwordHash("hash-seguro")
                .build();
        when(usuarioRepository.findByEmailIgnoreCase("ana@ejemplo.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash-seguro")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> authService.login(
                new LoginRequestDTO("ana@ejemplo.com", "incorrecta")));

        verifyNoInteractions(jwtEncoder);
    }
}
