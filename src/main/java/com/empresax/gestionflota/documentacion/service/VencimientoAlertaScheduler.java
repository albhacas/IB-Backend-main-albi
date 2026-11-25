package com.empresax.gestionflota.documentacion.service;

import com.empresax.gestionflota.chofer.model.CertificadoDelitos;
import com.empresax.gestionflota.chofer.model.Chofer;
import com.empresax.gestionflota.chofer.model.LicenciaChofer;
import com.empresax.gestionflota.chofer.repository.ChoferRepository;
import com.empresax.gestionflota.documentacion.model.DocumentoDigital;
import com.empresax.gestionflota.documentacion.repository.DocumentoDigitalRepository;
import com.empresax.gestionflota.notificacion.service.NotificationService;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.vehiculo.repository.VehiculoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class VencimientoAlertaScheduler {

    private final ChoferRepository choferRepository;
    private final VehiculoRepository vehiculoRepository;
    private final DocumentoDigitalRepository documentoRepository;
    private final NotificationService notificationService;

    public VencimientoAlertaScheduler(ChoferRepository choferRepository,
                                       VehiculoRepository vehiculoRepository,
                                       DocumentoDigitalRepository documentoRepository,
                                       NotificationService notificationService) {
        this.choferRepository = choferRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.documentoRepository = documentoRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 5 8 * * *")
    @Transactional(readOnly = true)
    public void notificarVencimientosGenerales() {
        LocalDate hoy = LocalDate.now();
        notificarChoferes(hoy);
        notificarDocumentosVehiculos(hoy);
    }

    private void notificarChoferes(LocalDate hoy) {
        LocalDate limiteCarnet = hoy.plusMonths(3); // 3 meses antes mínimo
        LocalDate limiteDelitos = hoy.plusDays(7);  // 1 semana antes

        for (Chofer chofer : choferRepository.findByActivoTrue()) {
            String nombreCompleto = chofer.getApellido() + " " + chofer.getNombre();

            // 1. Carnets de conducir (3 meses antes)
            for (LicenciaChofer lic : chofer.getLicencias()) {
                if (lic.getFechaVencimiento() == null) continue;
                LocalDate venc = lic.getFechaVencimiento();
                if (!venc.isAfter(limiteCarnet)) {
                    long dias = ChronoUnit.DAYS.between(hoy, venc);
                    String titulo = dias < 0
                            ? "Carnet " + lic.getTipoLicencia() + " VENCIDO"
                            : "Carnet " + lic.getTipoLicencia() + " próximo a vencer";
                    String mensaje = dias < 0
                            ? nombreCompleto + ": el carnet venció el " + venc + "."
                            : nombreCompleto + ": vence en " + dias + " día(s) (" + venc + ").";
                    notificationService.enviarNotificacionATodos(titulo, mensaje, "/choferes");
                }
            }

            // 2. Certificado de Delitos Sexuales (6 meses duración -> 7 días antes)
            CertificadoDelitos cert = chofer.getCertificadoDelitos();
            if (cert != null && cert.getFechaVencimiento() != null) {
                LocalDate venc = cert.getFechaVencimiento();
                if (!venc.isAfter(limiteDelitos) || "Vencido".equalsIgnoreCase(cert.getEstado())) {
                    long dias = ChronoUnit.DAYS.between(hoy, venc);
                    String titulo = dias < 0 || "Vencido".equalsIgnoreCase(cert.getEstado())
                            ? "Cert. Delitos Sexuales VENCIDO"
                            : "Cert. Delitos Sexuales vence esta semana";
                    String mensaje = dias < 0
                            ? nombreCompleto + ": el certificado venció el " + venc + "."
                            : nombreCompleto + ": vence en " + dias + " día(s) (" + venc + ").";
                    notificationService.enviarNotificacionATodos(titulo, mensaje, "/choferes");
                }
            }
        }
    }

    private void notificarDocumentosVehiculos(LocalDate hoy) {
        Map<Long, Vehiculo> vehiculosActivos = new HashMap<>();
        for (Vehiculo v : vehiculoRepository.findByActivoTrue()) {
            vehiculosActivos.put(v.getId(), v);
        }

        // Tomar solo el último documento por (vehiculoId + tipo)
        Map<String, DocumentoDigital> ultimosPorTipo = new HashMap<>();
        List<DocumentoDigital> docs = documentoRepository.findByCategoriaOrderByIdDesc("vehiculos");

        for (DocumentoDigital doc : docs) {
            if (!vehiculosActivos.containsKey(doc.getEntidadId()) || doc.getFechaVencimiento() == null) {
                continue;
            }
            String clave = doc.getEntidadId() + "::" + doc.getTipo().trim().toLowerCase();
            ultimosPorTipo.merge(clave, doc, (actual, candidato) ->
                    candidato.getFechaVencimiento().isAfter(actual.getFechaVencimiento()) ? candidato : actual);
        }

        for (DocumentoDigital doc : ultimosPorTipo.values()) {
            Vehiculo v = vehiculosActivos.get(doc.getEntidadId());
            String tipoLower = doc.getTipo().trim().toLowerCase();
            int diasAnticipacion = obtenerDiasAnticipacionVehiculo(tipoLower);
            if (diasAnticipacion <= 0) continue;

            LocalDate venc = doc.getFechaVencimiento();
            if (!venc.isAfter(hoy.plusDays(diasAnticipacion))) {
                long dias = ChronoUnit.DAYS.between(hoy, venc);
                boolean esSeguroAnual = tipoLower.equals("póliza") || tipoLower.equals("poliza") || tipoLower.equals("certificado seguro");

                String titulo = dias < 0
                        ? doc.getTipo() + " VENCIDO - Int. " + v.getInterno()
                        : (esSeguroAnual
                            ? "Pedir renovación de " + doc.getTipo() + " - Int. " + v.getInterno()
                            : doc.getTipo() + " próximo a vencer - Int. " + v.getInterno());

                String mensaje = dias < 0
                        ? "Unidad " + v.getPatente() + ": venció el " + venc + "."
                        : "Unidad " + v.getPatente() + ": vence en " + dias + " día(s) (" + venc + ").";

                notificationService.enviarNotificacionATodos(titulo, mensaje, "/documentacion");
            }
        }
    }

    private int obtenerDiasAnticipacionVehiculo(String tipoLower) {
        return switch (tipoLower) {
            case "rto provincial", "rto nacional" -> 10;
            case "póliza", "poliza", "certificado seguro", "recibo seguro" -> 15;
            default -> 15;
        };
    }
}