package com.empresax.gestionflota.usuario.dto;

public record LoginResponseDTO(String token, UsuarioResponseDTO usuario, long expiresAt) {
}
