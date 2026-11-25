package com.empresax.gestionflota.notificacion.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscriptionDTO {
    private String endpoint;
    private Map<String, String> keys;
    private String usuario;
}