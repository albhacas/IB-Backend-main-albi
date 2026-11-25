package com.empresax.gestionflota.vehiculo.service;

import com.empresax.gestionflota.notificacion.service.NotificationService;
import com.empresax.gestionflota.vehiculo.dto.MatafuegoAlertaDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatafuegoAlertaScheduler {

    private final VehiculoService vehiculoService;
    private final NotificationService notificationService;

    public MatafuegoAlertaScheduler(VehiculoService vehiculoService,
                                    NotificationService notificationService) {
        this.vehiculoService = vehiculoService;
        this.notificationService = notificationService;
    }

    // Se ejecuta todos los días a las 08:00 hs
    @Scheduled(cron = "0 0 8 * * *")
    public void notificarMatafuegosPorVencer() {
        List<MatafuegoAlertaDTO> alertas = vehiculoService.obtenerAlertasMatafuegos(VehiculoService.DIAS_AVISO_MATAFUEGO);

        for (MatafuegoAlertaDTO alerta : alertas) {
            String titulo = "VENCIDO".equals(alerta.getEstado())
                    ? "🧯 Matafuego VENCIDO - Int. " + alerta.getInterno()
                    : "🧯 Matafuego por vencer - Int. " + alerta.getInterno();

            String detalleTiempo = alerta.getDiasRestantes() < 0
                    ? "venció hace " + Math.abs(alerta.getDiasRestantes()) + " día(s)"
                    : (alerta.getDiasRestantes() == 0
                    ? "vence HOY"
                    : "vence en " + alerta.getDiasRestantes() + " día(s)");

            String mensaje = String.format("Unidad %s (%s) - %s %s (%s).",
                    alerta.getPatente(),
                    alerta.getUbicacion(),
                    detalleTiempo,
                    alerta.getFechaVencimiento(),
                    alerta.getNumeroSerie() != null ? "Serie " + alerta.getNumeroSerie() : "Sin serie");

            notificationService.enviarNotificacionATodos(titulo, mensaje, "/vehiculos");
        }
    }
}