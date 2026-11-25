package com.empresax.gestionflota.usuario.service;

import com.empresax.gestionflota.usuario.dto.UsuarioActualizacionDTO;
import com.empresax.gestionflota.usuario.dto.UsuarioRegistroDTO;
import com.empresax.gestionflota.usuario.dto.UsuarioResponseDTO;
import com.empresax.gestionflota.usuario.model.RolUsuario;
import com.empresax.gestionflota.usuario.model.Usuario;
import com.empresax.gestionflota.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void listaUsuariosConRolYSinExponerHashDeContrasena() {
        Usuario usuario = Usuario.builder()
                .id(7L)
                .nombre("Ana")
                .apellido("Pérez")
                .email("ana@ejemplo.com")
                .rol(RolUsuario.CHOFER)
                .passwordHash("hash-seguro")
                .build();
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<UsuarioResponseDTO> respuesta = service.listar();

        assertEquals(List.of(new UsuarioResponseDTO(
                7L, "Ana", "Pérez", RolUsuario.CHOFER, "ana@ejemplo.com")), respuesta);
    }

    @Test
    void registraUsuarioNormalizaCorreoYNoDevuelveLaContrasena() {
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setNombre(" Ana ");
        dto.setApellido(" Pérez ");
        dto.setEmail(" ANA@Ejemplo.com ");
        dto.setRol(RolUsuario.GERENTE_ADMINISTRATIVO);
        dto.setPassword("contrasenaSegura");

        when(usuarioRepository.existsByEmail("ana@ejemplo.com")).thenReturn(false);
        when(passwordEncoder.encode("contrasenaSegura")).thenReturn("hash-seguro");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(7L);
            return usuario;
        });

        UsuarioResponseDTO respuesta = service.registrar(dto);

        assertEquals(new UsuarioResponseDTO(
                7L, "Ana", "Pérez", RolUsuario.GERENTE_ADMINISTRATIVO, "ana@ejemplo.com"), respuesta);
        verify(passwordEncoder).encode("contrasenaSegura");
        verify(usuarioRepository).save(argThat(usuario ->
                "hash-seguro".equals(usuario.getPasswordHash())
                        && usuario.getRol() == RolUsuario.GERENTE_ADMINISTRATIVO
                        && !"contrasenaSegura".equals(usuario.getPasswordHash())));
    }

    @Test
    void registroInicialCreaGerenteAdministrativo() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.existsByEmail("ana@ejemplo.com")).thenReturn(false);
        when(passwordEncoder.encode("contrasenaSegura")).thenReturn("hash-seguro");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(8L);
            return usuario;
        });

        UsuarioResponseDTO respuesta = service.registrarInicial(
                "Ana", "Pérez", "ANA@Ejemplo.com", "contrasenaSegura");

        assertEquals(RolUsuario.GERENTE_ADMINISTRATIVO, respuesta.rol());
        assertEquals("ana@ejemplo.com", respuesta.email());
        verify(usuarioRepository).save(argThat(usuario ->
                usuario.getRol() == RolUsuario.GERENTE_ADMINISTRATIVO));
    }

    @Test
    void rechazaRegistroInicialCuandoYaHayUsuarios() {
        when(usuarioRepository.count()).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.registrarInicial(
                "Ana", "Pérez", "ana@ejemplo.com", "contrasenaSegura"));

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rechazaCorreoYaRegistrado() {
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setNombre("Ana");
        dto.setApellido("Pérez");
        dto.setEmail("ana@ejemplo.com");
        dto.setRol(RolUsuario.CHOFER);
        dto.setPassword("contrasenaSegura");
        when(usuarioRepository.existsByEmail("ana@ejemplo.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.registrar(dto));
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rechazaContrasenaMenorA8Caracteres() {
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setNombre("Ana");
        dto.setApellido("Pérez");
        dto.setEmail("ana@ejemplo.com");
        dto.setRol(RolUsuario.CHOFER);
        dto.setPassword("corta");

        assertThrows(IllegalArgumentException.class, () -> service.registrar(dto));
        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    void actualizaDatosSinCambiarLaContrasenaCuandoSeOmite() {
        Usuario existente = Usuario.builder()
                .id(7L)
                .nombre("Ana")
                .apellido("Pérez")
                .email("ana@ejemplo.com")
                .rol(RolUsuario.CHOFER)
                .passwordHash("hash-anterior")
                .build();
        UsuarioActualizacionDTO dto = new UsuarioActualizacionDTO();
        dto.setNombre("Ana María");
        dto.setApellido("Pérez");
        dto.setEmail(" ANA@Ejemplo.com ");
        dto.setRol(RolUsuario.GERENTE_ADMINISTRATIVO);
        dto.setPassword(null);

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailAndIdNot("ana@ejemplo.com", 7L)).thenReturn(false);
        when(usuarioRepository.save(existente)).thenReturn(existente);

        UsuarioResponseDTO respuesta = service.actualizar(7L, dto);

        assertEquals(new UsuarioResponseDTO(
                7L, "Ana María", "Pérez", RolUsuario.GERENTE_ADMINISTRATIVO, "ana@ejemplo.com"), respuesta);
        assertEquals("hash-anterior", existente.getPasswordHash());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void eliminaUsuarioExistente() {
        Usuario existente = Usuario.builder().id(7L).build();
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(existente));

        service.eliminar(7L);

        verify(usuarioRepository).delete(existente);
    }
}
