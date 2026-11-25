package com.empresax.gestionflota.usuario.dto;

import com.empresax.gestionflota.usuario.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioActualizacionDTO {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String apellido;

    @NotNull
    private RolUsuario rol;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 72)
    private String password;
}
