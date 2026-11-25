package com.empresax.gestionflota.usuario.dto;

import com.empresax.gestionflota.usuario.model.RolUsuario;

public record UsuarioResponseDTO(Long id, String nombre, String apellido, RolUsuario rol, String email) {
}
