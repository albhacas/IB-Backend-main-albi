package com.empresax.gestionflota.usuario.service;

import com.empresax.gestionflota.usuario.dto.UsuarioRegistroDTO;
import com.empresax.gestionflota.usuario.dto.UsuarioActualizacionDTO;
import com.empresax.gestionflota.usuario.dto.UsuarioResponseDTO;
import com.empresax.gestionflota.usuario.model.RolUsuario;
import com.empresax.gestionflota.usuario.model.Usuario;
import com.empresax.gestionflota.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRegistroDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        }
        if (dto.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        return registrar(dto, dto.getRol());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UsuarioResponseDTO registrarInicial(String nombre, String apellido, String email, String password) {
        if (usuarioRepository.count() > 0) {
            throw new IllegalStateException("El registro inicial ya fue realizado.");
        }

        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setEmail(email);
        dto.setPassword(password);
        return registrar(dto, RolUsuario.GERENTE_ADMINISTRATIVO);
    }

    @Transactional(readOnly = true)
    public boolean registroInicialDisponible() {
        return usuarioRepository.count() == 0;
    }

    private UsuarioResponseDTO registrar(UsuarioRegistroDTO dto, RolUsuario rol) {
        String nombre = normalizar(dto.getNombre(), "El nombre es obligatorio.");
        String apellido = normalizar(dto.getApellido(), "El apellido es obligatorio.");
        String email = normalizar(dto.getEmail(), "El correo electrónico es obligatorio.")
                .toLowerCase(Locale.ROOT);
        String password = dto.getPassword();

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        validarPassword(password);
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo electrónico.");
        }

        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .apellido(apellido)
                .rol(rol)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioActualizacionDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        }

        Usuario usuario = buscarPorId(id);
        String nombre = normalizar(dto.getNombre(), "El nombre es obligatorio.");
        String apellido = normalizar(dto.getApellido(), "El apellido es obligatorio.");
        String email = normalizar(dto.getEmail(), "El correo electrónico es obligatorio.")
                .toLowerCase(Locale.ROOT);

        if (dto.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }
        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo electrónico.");
        }

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setRol(dto.getRol());

        String password = dto.getPassword();
        if (password != null && !password.isBlank()) {
            validarPassword(password);
            usuario.setPasswordHash(passwordEncoder.encode(password));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.delete(buscarPorId(id));
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el usuario con ID: " + id));
    }

    private void validarPassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar los 72 bytes.");
        }
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getEmail());
    }

    private String normalizar(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
