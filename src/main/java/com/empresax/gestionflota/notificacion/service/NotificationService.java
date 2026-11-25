package com.empresax.gestionflota.notificacion.service;

import com.empresax.gestionflota.notificacion.dto.PushSubscriptionDTO;
import com.empresax.gestionflota.notificacion.model.PushSubscriptionEntity;
import com.empresax.gestionflota.notificacion.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Security;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Value("${webpush.vapid.public-key}")
    private String publicKey;

    @Value("${webpush.vapid.private-key}")
    private String privateKey;

    @Value("${webpush.vapid.subject}")
    private String subject;

    private final PushSubscriptionRepository repository;
    private PushService pushService;

    public NotificationService(PushSubscriptionRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() throws Exception {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        this.pushService = new PushService(publicKey, privateKey, subject);
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    @Transactional
    public void guardarSuscripcion(PushSubscriptionDTO dto) {
        if (dto.getEndpoint() == null || dto.getKeys() == null) {
            throw new IllegalArgumentException("Datos de suscripción incompletos.");
        }

        String p256dh = dto.getKeys().get("p256dh");
        String auth = dto.getKeys().get("auth");

        repository.findByEndpoint(dto.getEndpoint()).ifPresentOrElse(
                existente -> {
                    existente.setP256dh(p256dh);
                    existente.setAuth(auth);
                    existente.setUsuario(dto.getUsuario() != null ? dto.getUsuario() : "operador");
                    repository.save(existente);
                },
                () -> {
                    PushSubscriptionEntity nueva = PushSubscriptionEntity.builder()
                            .endpoint(dto.getEndpoint())
                            .p256dh(p256dh)
                            .auth(auth)
                            .usuario(dto.getUsuario() != null ? dto.getUsuario() : "operador")
                            .build();
                    repository.save(nueva);
                }
        );
    }

    @Async
    public void enviarNotificacionATodos(String titulo, String mensaje, String urlDestino) {
        String payload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"url\":\"%s\"}",
                titulo.replace("\"", "\\\""),
                mensaje.replace("\"", "\\\""),
                urlDestino != null ? urlDestino : "/"
        );

        List<PushSubscriptionEntity> suscripciones = repository.findAll();

        for (PushSubscriptionEntity subEntity : suscripciones) {
            try {
                Subscription sub = new Subscription(
                        subEntity.getEndpoint(),
                        new Subscription.Keys(subEntity.getP256dh(), subEntity.getAuth())
                );
                Notification notification = new Notification(sub, payload);
                pushService.send(notification);
            } catch (Exception e) {
                // Si el endpoint caducó (código 404 o 410 Gone), se remueve
                if (e.getMessage() != null && (e.getMessage().contains("404") || e.getMessage().contains("410"))) {
                    repository.deleteById(subEntity.getId());
                } else {
                    logger.warn("No se pudo enviar notificación push a la suscripción {}", subEntity.getId(), e);
                }
            }
        }
    }
}