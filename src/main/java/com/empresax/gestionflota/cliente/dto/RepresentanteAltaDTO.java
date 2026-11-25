package com.empresax.gestionflota.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepresentanteAltaDTO {

    @Email
    private String email;

    private Long telefono;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String dni;
}
