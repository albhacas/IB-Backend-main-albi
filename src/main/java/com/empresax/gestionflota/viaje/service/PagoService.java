package com.empresax.gestionflota.viaje.service;

import com.empresax.gestionflota.viaje.dto.PagoAltaDTO;
import com.empresax.gestionflota.viaje.dto.PagoResponseDTO;
import com.empresax.gestionflota.viaje.model.*;
import com.empresax.gestionflota.viaje.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoService {

    private final ViajeRepository viajeRepository;
    private final PagoRepository pagoRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final ConceptoPagoRepository conceptoPagoRepository;

    public PagoService(ViajeRepository viajeRepository,
                       PagoRepository pagoRepository,
                       MedioPagoRepository medioPagoRepository,
                       ConceptoPagoRepository conceptoPagoRepository) {
        this.viajeRepository = viajeRepository;
        this.pagoRepository = pagoRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.conceptoPagoRepository = conceptoPagoRepository;
    }

    // REGISTRAR COBRO / PAGO
    @Transactional
    public PagoResponseDTO registrarPago(Long viajeId, PagoAltaDTO dto) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el viaje #" + viajeId));

        if (!viaje.getActivo()) {
            throw new IllegalStateException("No se pueden registrar cobros en un viaje inactivo o dado de baja.");
        }

        String estadoNom = viaje.getEstado() != null ? viaje.getEstado().getNombre().toUpperCase() : "";
        if ("CANCELADO".equals(estadoNom) || "FINALIZADO".equals(estadoNom)) {
            throw new IllegalStateException("No se pueden registrar pagos en un viaje en estado " + estadoNom);
        }

        // Obtener o inferir Concepto (si aún no se pagó la seña, por defecto es Seña)
        String nombreConcepto = dto.getNombreConcepto();
        if (nombreConcepto == null || nombreConcepto.isBlank()) {
            nombreConcepto = Boolean.TRUE.equals(viaje.getSeniaPagada()) ? "Saldo" : "Seña";
        }

        final String conceptoFinal = nombreConcepto.trim();
        if (Boolean.TRUE.equals(viaje.getSeniaPagada()) && esConceptoSenia(conceptoFinal)) {
            throw new IllegalArgumentException("La seña de este viaje ya fue registrada. Los próximos cobros deben imputarse al saldo.");
        }

        ConceptoPago concepto = conceptoPagoRepository.findByNombreIgnoreCase(conceptoFinal)
                .orElseGet(() -> conceptoPagoRepository.save(ConceptoPago.builder().nombre(conceptoFinal).build()));

        // Obtener o inicializar Medio de Pago
        String nombreMedio = (dto.getNombreMedioPago() != null && !dto.getNombreMedioPago().isBlank())
                ? dto.getNombreMedioPago().trim()
                : "Efectivo";

        MedioPago medioPago = medioPagoRepository.findByNombreIgnoreCase(nombreMedio)
                .orElseGet(() -> medioPagoRepository.save(MedioPago.builder().nombre(nombreMedio).build()));

        // Invocar método del dominio de Viaje
        Pago pagoCreado = viaje.registrarPago(
                dto.getMonto(),
                medioPago,
                concepto,
                dto.getFechaHora() != null ? dto.getFechaHora() : LocalDateTime.now(),
                dto.getNroComprobante(),
                dto.getObservaciones()
        );

        viajeRepository.save(viaje);

        return toResponseDTO(pagoCreado, viaje);
    }

    private boolean esConceptoSenia(String concepto) {
        return "Seña".equalsIgnoreCase(concepto) || "Senia".equalsIgnoreCase(concepto);
    }

    // CONSULTAR COBROS DE UN VIAJE
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> consultarPagosPorViaje(Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el viaje #" + viajeId));

        return pagoRepository.findByViajeIdOrderByFechaHoraDesc(viajeId).stream()
                .map(pago -> toResponseDTO(pago, viaje))
                .toList();
    }

    // CONSULTAR TODOS LOS COBROS (Reporte general / Caja)
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> consultarTodosLosPagos() {
        return pagoRepository.findAllByOrderByFechaHoraDesc().stream()
                .map(pago -> toResponseDTO(pago, pago.getViaje()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedioPago> listarMediosPago() {
        return medioPagoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ConceptoPago> listarConceptosPago() {
        return conceptoPagoRepository.findAll();
    }

    private PagoResponseDTO toResponseDTO(Pago pago, Viaje viaje) {
        return PagoResponseDTO.builder()
                .id(pago.getId())
                .viajeId(viaje != null ? viaje.getId() : null)
                .fechaHora(pago.getFechaHora())
                .monto(pago.getMonto())
                .medioPago(pago.getMedioPago() != null ? pago.getMedioPago().getNombre() : "Desconocido")
                .concepto(pago.getConcepto() != null ? pago.getConcepto().getNombre() : "Desconocido")
                .nroComprobante(pago.getNroComprobante())
                .observaciones(pago.getObservaciones())
                .saldoRestanteViaje(viaje != null ? viaje.getSaldoPendiente() : null)
                .estadoViajeResultante(viaje != null && viaje.getEstado() != null ? viaje.getEstado().getNombre() : null)
                .build();
    }
}