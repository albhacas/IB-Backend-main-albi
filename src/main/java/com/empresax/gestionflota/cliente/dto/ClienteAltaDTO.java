package com.empresax.gestionflota.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteAltaDTO {

    @NotBlank
    private String razonSocial;

    @NotBlank
    private String cuit;

    private String direccion;
    private Long telefono;

    @Email
    private String email;

    @NotNull
    private Long representanteId;
}
