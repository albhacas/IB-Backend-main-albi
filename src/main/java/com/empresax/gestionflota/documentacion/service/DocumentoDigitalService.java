package com.empresax.gestionflota.documentacion.service;

import com.empresax.gestionflota.documentacion.dto.DocumentoDigitalDTO;
import com.empresax.gestionflota.documentacion.model.DocumentoDigital;
import com.empresax.gestionflota.documentacion.repository.DocumentoDigitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentoDigitalService {

    private final DocumentoDigitalRepository repository;

    public DocumentoDigitalService(DocumentoDigitalRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DocumentoDigital> listar(String categoria, Long entidadId) {
        if (categoria != null && !categoria.isBlank() && entidadId != null) {
            return repository.findByCategoriaAndEntidadIdOrderByIdDesc(categoria.trim().toLowerCase(), entidadId);
        }
        if (categoria != null && !categoria.isBlank()) {
            return repository.findByCategoriaOrderByIdDesc(categoria.trim().toLowerCase());
        }
        return repository.findAll();
    }

    @Transactional
    public DocumentoDigital registrar(DocumentoDigitalDTO dto) {
        if (dto.getCategoria() == null || dto.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        if (dto.getEntidadId() == null) {
            throw new IllegalArgumentException("La entidad asociada es obligatoria.");
        }
        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        LocalDate emision = dto.getFechaEmision() != null ? dto.getFechaEmision() : LocalDate.now();
        String tipoNormalizado = dto.getTipo().trim();
        LocalDate vencimiento = resolverVencimientoSegunRegla(tipoNormalizado, emision, dto.getFechaVencimiento());

        if (vencimiento != null && vencimiento.isBefore(emision)) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de emisión.");
        }

        DocumentoDigital doc = DocumentoDigital.builder()
                .categoria(dto.getCategoria().trim().toLowerCase())
                .entidadId(dto.getEntidadId())
                .tipo(tipoNormalizado)
                .fechaEmision(emision)
                .fechaVencimiento(vencimiento)
                .archivoNombre(dto.getArchivoNombre())
                .formato(dto.getFormato() != null && !dto.getFormato().isBlank() ? dto.getFormato() : "PDF")
                .fechaCreacion(LocalDateTime.now())
                .build();

        return repository.save(doc);
    }

    @Transactional
    public DocumentoDigital registrarConArchivo(String categoria,
                                                Long entidadId,
                                                String tipo,
                                                LocalDate fechaEmision,
                                                LocalDate fechaVencimiento,
                                                org.springframework.web.multipart.MultipartFile archivo) throws java.io.IOException {
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        if (entidadId == null) {
            throw new IllegalArgumentException("La entidad asociada es obligatoria.");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        LocalDate emision = fechaEmision != null ? fechaEmision : LocalDate.now();
        String tipoNormalizado = tipo.trim();
        LocalDate vencimiento = resolverVencimientoSegunRegla(tipoNormalizado, emision, fechaVencimiento);

        if (vencimiento != null && vencimiento.isBefore(emision)) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de emisión.");
        }

        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        String nombreArchivo = hayArchivo && archivo.getOriginalFilename() != null
                ? archivo.getOriginalFilename()
                : tipoNormalizado.replaceAll("\\s+", "_") + ".pdf";

        String contentType = hayArchivo && archivo.getContentType() != null
                ? archivo.getContentType()
                : "application/pdf";

        String formato = contentType.startsWith("image/") ? "IMG" : "PDF";

        DocumentoDigital doc = DocumentoDigital.builder()
                .categoria(categoria.trim().toLowerCase())
                .entidadId(entidadId)
                .tipo(tipoNormalizado)
                .fechaEmision(emision)
                .fechaVencimiento(vencimiento)
                .archivoNombre(nombreArchivo)
                .formato(formato)
                .tipoContenido(contentType)
                .tamanioBytes(hayArchivo ? archivo.getSize() : 0L)
                .tieneArchivo(hayArchivo)
                .contenido(hayArchivo ? archivo.getBytes() : null)
                .fechaCreacion(LocalDateTime.now())
                .build();

        return repository.save(doc);
    }

    @Transactional(readOnly = true)
    public DocumentoDigital buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el documento con ID: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el documento con ID: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Reglas de negocio automáticas:
     * - Certificado seguro: vence todos los 30 de abril de cada año.
     * - Recibo seguro: vence mensualmente (si no se especifica, 1 mes desde emisión).
     */
    public LocalDate resolverVencimientoSegunRegla(String tipo, LocalDate emision, LocalDate vencimientoIngresado) {
        String t = tipo.toLowerCase();

        if (t.equals("certificado seguro")) {
            return calcularProximo30DeAbril(emision);
        }

        if (t.equals("recibo seguro")) {
            return vencimientoIngresado != null ? vencimientoIngresado : emision.plusMonths(1);
        }

        return vencimientoIngresado;
    }

    public static LocalDate calcularProximo30DeAbril(LocalDate fechaBase) {
        LocalDate base = fechaBase != null ? fechaBase : LocalDate.now();
        LocalDate abril30AnioBase = LocalDate.of(base.getYear(), 4, 30);
        return !base.isAfter(abril30AnioBase)
                ? abril30AnioBase
                : LocalDate.of(base.getYear() + 1, 4, 30);
    }
}