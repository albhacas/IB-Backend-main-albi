package com.empresax.gestionflota.chofer.service;

import com.empresax.gestionflota.chofer.dto.ChoferAltaDTO;
import com.empresax.gestionflota.chofer.model.CertificadoDelitos;
import com.empresax.gestionflota.chofer.model.Chofer;
import com.empresax.gestionflota.chofer.model.LicenciaChofer;
import com.empresax.gestionflota.chofer.repository.ChoferRepository;
import com.empresax.gestionflota.documentacion.model.DocumentoDigital;
import com.empresax.gestionflota.documentacion.repository.DocumentoDigitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChoferService {

    private final ChoferRepository choferRepository;
    private final DocumentoDigitalRepository documentoDigitalRepository;

    public ChoferService(ChoferRepository choferRepository,
                         DocumentoDigitalRepository documentoDigitalRepository) {
        this.choferRepository = choferRepository;
        this.documentoDigitalRepository = documentoDigitalRepository;
    }

    @Transactional(readOnly = true)
    public List<Chofer> listarTodos(Boolean soloActivos) {
        if (Boolean.TRUE.equals(soloActivos)) {
            return choferRepository.findByActivoTrue();
        }
        return choferRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Chofer buscarPorId(Long id) {
        return choferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el chofer con ID: " + id));
    }

    // IG1-21: Registrar Chofer
    @Transactional
    public Chofer registrarDesdeDTO(ChoferAltaDTO dto) {
        return registrarDesdeDTO(dto, null, null, null);
    }

    @Transactional
    public Chofer registrarDesdeDTO(ChoferAltaDTO dto,
                                   MultipartFile carnetProvincial,
                                   MultipartFile carnetNacional,
                                   MultipartFile certificadoDelitos) {
        validarDatosChofer(dto, null);

        String dniNormalizado = dto.getDni().trim();

        Chofer chofer = Chofer.builder()
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .dni(dniNormalizado)
                .cuil(dto.getCuil().trim())
                .fechaNacimiento(dto.getFechaNacimiento())
                .telefono(dto.getTelefono().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .domicilio(dto.getDomicilio())
                .localidad(dto.getLocalidad())
                .provincia(dto.getProvincia())
                .fotoUrl(dto.getFotoUrl())
                .ambitoHabilitacion(dto.getAmbitoHabilitacion())
                .turnoDisponibilidad(dto.getTurnoDisponibilidad())
                .estado(dto.getEstadoInicial() != null ? dto.getEstadoInicial() : "Activo - Habilitado para Asignación")
                .activo(true)
                .build();

        boolean requiereProvincial = "Ambos".equalsIgnoreCase(dto.getAmbitoHabilitacion())
                || "Solo Provincial".equalsIgnoreCase(dto.getAmbitoHabilitacion());

        if (requiereProvincial) {
            String nroProv = (dto.getNroCarnetProvincial() != null && !dto.getNroCarnetProvincial().isBlank())
                    ? dto.getNroCarnetProvincial().trim()
                    : dniNormalizado;

            LicenciaChofer licProv = LicenciaChofer.builder()
                    .tipoLicencia("PROVINCIAL")
                    .numeroLicencia(nroProv)
                    .categoria(dto.getCategoriaProvincial())
                    .fechaVencimiento(dto.getVencimientoProvincial() != null ? dto.getVencimientoProvincial() : LocalDate.now().plusYears(1))
                    .build();
            chofer.addLicencia(licProv);
        }

        boolean requiereNacional = "Ambos".equalsIgnoreCase(dto.getAmbitoHabilitacion())
                || "Solo Nacional".equalsIgnoreCase(dto.getAmbitoHabilitacion());

        if (requiereNacional) {
            String nroNac = (dto.getNroCarnetNacional() != null && !dto.getNroCarnetNacional().isBlank())
                    ? dto.getNroCarnetNacional().trim()
                    : dniNormalizado;

            LicenciaChofer licNac = LicenciaChofer.builder()
                    .tipoLicencia("NACIONAL")
                    .numeroLicencia(nroNac)
                    .categoria(dto.getCategoriaNacional())
                    .fechaVencimiento(dto.getVencimientoNacional() != null ? dto.getVencimientoNacional() : LocalDate.now().plusYears(1))
                    .build();
            chofer.addLicencia(licNac);
        }

        if (dto.getFechaEmisionDelitos() != null) {
            LocalDate vencimiento = dto.getFechaVencimientoDelitos();
            if (vencimiento == null) {
                vencimiento = dto.getFechaEmisionDelitos().plusMonths(6);
            }

            CertificadoDelitos certificado = CertificadoDelitos.builder()
                    .fechaEmision(dto.getFechaEmisionDelitos())
                    .fechaVencimiento(vencimiento)
                    .estado(dto.getEstadoDelitos() != null ? dto.getEstadoDelitos() : "Vigente")
                    .archivoUrl(dto.getArchivoDelitosUrl())
                    .build();
            chofer.setCertificadoDelitos(certificado);
        }

        Chofer guardado = choferRepository.save(chofer);
        sincronizarDocumentacion(guardado, carnetProvincial, carnetNacional, certificadoDelitos);
        return guardado;
    }

    // IG1-22: Modificar Chofer
    @Transactional
    public Chofer actualizarDesdeDTO(Long id, ChoferAltaDTO dto) {
        return actualizarDesdeDTO(id, dto, null, null, null);
    }

    @Transactional
    public Chofer actualizarDesdeDTO(Long id,
                                    ChoferAltaDTO dto,
                                    MultipartFile carnetProvincial,
                                    MultipartFile carnetNacional,
                                    MultipartFile certificadoDelitos) {
        validarDatosChofer(dto, id);

        Chofer chofer = buscarPorId(id);
        String dniNormalizado = dto.getDni().trim();

        chofer.setDni(dniNormalizado);
        chofer.setNombre(dto.getNombre().trim());
        chofer.setApellido(dto.getApellido().trim());
        chofer.setCuil(dto.getCuil().trim());
        chofer.setFechaNacimiento(dto.getFechaNacimiento());
        chofer.setTelefono(dto.getTelefono().trim());
        chofer.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        chofer.setDomicilio(dto.getDomicilio());
        chofer.setLocalidad(dto.getLocalidad());
        chofer.setProvincia(dto.getProvincia());
        if (dto.getFotoUrl() != null && !dto.getFotoUrl().isBlank()) {
            chofer.setFotoUrl(dto.getFotoUrl());
        }
        chofer.setAmbitoHabilitacion(dto.getAmbitoHabilitacion());
        chofer.setTurnoDisponibilidad(dto.getTurnoDisponibilidad());
        if (dto.getEstadoInicial() != null) {
            chofer.setEstado(dto.getEstadoInicial());
        }

        String nroProv = (dto.getNroCarnetProvincial() != null && !dto.getNroCarnetProvincial().isBlank())
                ? dto.getNroCarnetProvincial().trim()
                : dniNormalizado;

        String nroNac = (dto.getNroCarnetNacional() != null && !dto.getNroCarnetNacional().isBlank())
                ? dto.getNroCarnetNacional().trim()
                : dniNormalizado;

        actualizarOLicencia(chofer, "PROVINCIAL", nroProv, dto.getCategoriaProvincial(), dto.getVencimientoProvincial());
        actualizarOLicencia(chofer, "NACIONAL", nroNac, dto.getCategoriaNacional(), dto.getVencimientoNacional());

        if (dto.getFechaEmisionDelitos() != null) {
            LocalDate vencimiento = dto.getFechaVencimientoDelitos();
            if (vencimiento == null) {
                vencimiento = dto.getFechaEmisionDelitos().plusMonths(6);
            }

            if (chofer.getCertificadoDelitos() == null) {
                CertificadoDelitos certificado = CertificadoDelitos.builder()
                        .fechaEmision(dto.getFechaEmisionDelitos())
                        .fechaVencimiento(vencimiento)
                        .estado(dto.getEstadoDelitos() != null ? dto.getEstadoDelitos() : "Vigente")
                        .archivoUrl(dto.getArchivoDelitosUrl())
                        .build();
                chofer.setCertificadoDelitos(certificado);
            } else {
                chofer.getCertificadoDelitos().setFechaEmision(dto.getFechaEmisionDelitos());
                chofer.getCertificadoDelitos().setFechaVencimiento(vencimiento);
                if (dto.getEstadoDelitos() != null) {
                    chofer.getCertificadoDelitos().setEstado(dto.getEstadoDelitos());
                }
                if (dto.getArchivoDelitosUrl() != null && !dto.getArchivoDelitosUrl().isBlank()) {
                    chofer.getCertificadoDelitos().setArchivoUrl(dto.getArchivoDelitosUrl());
                }
            }
        }

        Chofer guardado = choferRepository.save(chofer);
        sincronizarDocumentacion(guardado, carnetProvincial, carnetNacional, certificadoDelitos);
        return guardado;
    }

    private void sincronizarDocumentacion(Chofer chofer,
                                          MultipartFile carnetProvincial,
                                          MultipartFile carnetNacional,
                                          MultipartFile certificadoDelitos) {
        for (LicenciaChofer licencia : chofer.getLicencias()) {
            String tipoDocumento;
            MultipartFile archivo;
            if ("PROVINCIAL".equalsIgnoreCase(licencia.getTipoLicencia())) {
                tipoDocumento = "Licencia de Conducir Provincial";
                archivo = carnetProvincial;
            } else if ("NACIONAL".equalsIgnoreCase(licencia.getTipoLicencia())) {
                tipoDocumento = "Licencia de Conducir Nacional";
                archivo = carnetNacional;
            } else {
                continue;
            }

            guardarDocumentoChofer(chofer.getId(), tipoDocumento, LocalDate.now(),
                    licencia.getFechaVencimiento(), null, licencia.getNumeroLicencia(), archivo);
        }

        CertificadoDelitos certificado = chofer.getCertificadoDelitos();
        if (certificado != null) {
            guardarDocumentoChofer(chofer.getId(), "Certificado de Delitos Sexuales",
                    certificado.getFechaEmision(), certificado.getFechaVencimiento(),
                    certificado.getEstado(), null, certificadoDelitos);
        }
    }

    private void guardarDocumentoChofer(Long choferId,
                                        String tipo,
                                        LocalDate fechaEmision,
                                        LocalDate fechaVencimiento,
                                        String estado,
                                        String nro,
                                        MultipartFile archivo) {
        DocumentoDigital documento = documentoDigitalRepository
                .findFirstByCategoriaAndEntidadIdAndTipoIgnoreCaseOrderByIdDesc(
                        "choferes", choferId, tipo)
                .orElseGet(() -> DocumentoDigital.builder()
                        .categoria("choferes")
                        .entidadId(choferId)
                        .tipo(tipo)
                        .fechaCreacion(LocalDateTime.now())
                        .build());

        documento.setFechaEmision(fechaEmision);
        documento.setFechaVencimiento(fechaVencimiento);
        documento.setEstado(estado);
        documento.setNro(nro);
        if (archivo != null && !archivo.isEmpty()) {
            try {
                String nombre = archivo.getOriginalFilename() != null
                        ? archivo.getOriginalFilename()
                        : tipo.replaceAll("\\s+", "_");
                String tipoContenido = archivo.getContentType() != null
                        ? archivo.getContentType()
                        : "application/octet-stream";
                documento.setArchivoNombre(nombre);
                documento.setTipoContenido(tipoContenido);
                documento.setTamanioBytes(archivo.getSize());
                documento.setTieneArchivo(true);
                documento.setContenido(archivo.getBytes());
                documento.setFormato(tipoContenido.startsWith("image/") ? "IMG" : "PDF");
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer el archivo de " + tipo + ".", e);
            }
        }
        documentoDigitalRepository.save(documento);
    }

    private void validarDatosChofer(ChoferAltaDTO dto, Long idExcluido) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del chofer son obligatorios.");
        }

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (dto.getApellido() == null || dto.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }

        if (dto.getDni() == null || !dto.getDni().trim().matches("^\\d{7,8}$")) {
            throw new IllegalArgumentException("El DNI debe ser numérico y contener entre 7 y 8 dígitos.");
        }
        Optional<Chofer> existenteDni = choferRepository.findByDni(dto.getDni().trim());
        if (existenteDni.isPresent() && (idExcluido == null || !existenteDni.get().getId().equals(idExcluido))) {
            throw new IllegalArgumentException("Ya existe un chofer registrado con el DNI: " + dto.getDni().trim());
        }

        if (dto.getCuil() == null || !dto.getCuil().trim().matches("^\\d{2}-\\d{7,8}-\\d$")) {
            throw new IllegalArgumentException("El CUIL debe tener un formato válido con guiones (ej. 20-28450912-4).");
        }

        if (dto.getTelefono() == null || !dto.getTelefono().trim().matches("^\\+?[0-9\\s-]{8,20}$")) {
            throw new IllegalArgumentException("El teléfono debe contener entre 8 y 20 caracteres numéricos válidos.");
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!dto.getEmail().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("El correo electrónico ingresado no tiene un formato válido (ej. usuario@dominio.com).");
            }
        }
    }

    private void actualizarOLicencia(Chofer chofer, String tipo, String numero, String categoria, LocalDate vencimiento) {
        if (numero == null || numero.isBlank()) return;

        LicenciaChofer lic = chofer.getLicencias().stream()
                .filter(l -> tipo.equalsIgnoreCase(l.getTipoLicencia()))
                .findFirst().orElse(null);

        if (lic != null) {
            lic.setNumeroLicencia(numero.trim());
            lic.setCategoria(categoria);
            if (vencimiento != null) lic.setFechaVencimiento(vencimiento);
        } else {
            LicenciaChofer nueva = LicenciaChofer.builder()
                    .tipoLicencia(tipo)
                    .numeroLicencia(numero.trim())
                    .categoria(categoria)
                    .fechaVencimiento(vencimiento != null ? vencimiento : LocalDate.now().plusYears(1))
                    .build();
            chofer.addLicencia(nueva);
        }
    }

    // IG1-24: Baja de Chofer
    @Transactional
    public Chofer darDeBaja(Long id, String motivo) {
        Chofer chofer = buscarPorId(id);
        chofer.setActivo(false);
        chofer.setEstado("Baja Administrativa");
        chofer.setMotivoBaja(motivo != null && !motivo.isBlank() ? motivo : "Sin motivo indicado");
        chofer.setFechaBaja(LocalDate.now());
        return choferRepository.save(chofer);
    }

    // IG1-25: Reactivar / Dar de alta a Chofer
    @Transactional
    public Chofer darDeAlta(Long id) {
        Chofer chofer = buscarPorId(id);
        chofer.setActivo(true);
        chofer.setEstado("Activo - Habilitado para Asignación");
        chofer.setMotivoBaja(null);
        chofer.setFechaBaja(null);
        return choferRepository.save(chofer);
    }
}