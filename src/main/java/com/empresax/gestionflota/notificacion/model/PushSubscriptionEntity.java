package com.empresax.gestionflota.notificacion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000, unique = true)
    private String endpoint;

    @Column(nullable = false, length = 255)
    private String p256dh;

    @Column(nullable = false, length = 255)
    private String auth;

    @Column(length = 100)
    private String usuario; // ej: "admin", "despacho" o el DNI del chofer

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}