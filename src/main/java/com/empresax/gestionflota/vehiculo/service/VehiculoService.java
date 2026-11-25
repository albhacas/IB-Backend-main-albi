package com.empresax.gestionflota.vehiculo.service;

import com.empresax.gestionflota.vehiculo.dto.MatafuegoAlertaDTO;
import com.empresax.gestionflota.vehiculo.dto.MatafuegoDTO;
import com.empresax.gestionflota.vehiculo.model.Matafuego;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.vehiculo.repository.MatafuegoRepository;
import com.empresax.gestionflota.vehiculo.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehiculoService {

    public static final int DIAS_AVISO_MATAFUEGO = 10;

    private final VehiculoRepository vehiculoRepository;
    private final MatafuegoRepository matafuegoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           MatafuegoRepository matafuegoRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.matafuegoRepository = matafuegoRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listarTodos(Boolean soloActivos) {
        if (Boolean.TRUE.equals(soloActivos)) {
            return vehiculoRepository.findByActivoTrue();
        }
        return vehiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehiculo buscarPorId(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el vehículo con ID: " + id));
    }

    @Transactional
    public Vehiculo registrar(Vehiculo vehiculo) {
        vehiculo.setTipoVehiculo(normalizarYValidarTipoVehiculo(vehiculo.getTipoVehiculo()));
        validarReglasDeNegocio(vehiculo, null);
        String patenteNorm = vehiculo.getPatente().replaceAll("\\s+", "").toUpperCase();
        if (vehiculoRepository.existsByPatente(patenteNorm)) {
            throw new IllegalArgumentException("Ya existe una unidad registrada con la patente: " + patenteNorm);
        }
        vehiculo.setPatente(patenteNorm);
        vehiculo.setActivo(true);
        if (vehiculo.getEstado() == null || vehiculo.getEstado().isBlank()) {
            vehiculo.setEstado("DISPONIBLE");
        }

        if (vehiculo.getMatafuegos() != null) {
            List<Matafuego> copia = new ArrayList<>(vehiculo.getMatafuegos());
            vehiculo.getMatafuegos().clear();
            for (Matafuego matafuego : copia) {
                vehiculo.addMatafuego(matafuego);
            }
        }
        validarMatafuegos(vehiculo.getTipoVehiculo(), vehiculo.getMatafuegos());

        return vehiculoRepository.save(vehiculo);
    }

    // NUEVO: Modificar Vehículo
    @Transactional
    public Vehiculo actualizar(Long id, Vehiculo datosNuevos) {
        String tipoNormalizado = normalizarYValidarTipoVehiculo(datosNuevos.getTipoVehiculo());
        validarReglasDeNegocio(datosNuevos, id);

        Vehiculo existente = buscarPorId(id);
        String patenteNorm = datosNuevos.getPatente().replaceAll("\\s+", "").toUpperCase();

        if (vehiculoRepository.existsByPatenteAndIdNot(patenteNorm, id)) {
            throw new IllegalArgumentException("Ya existe otra unidad con la patente: " + patenteNorm);
        }

        existente.setInterno(datosNuevos.getInterno());
        existente.setPatente(patenteNorm);
        existente.setChapaProvincial(datosNuevos.getChapaProvincial() != null && !datosNuevos.getChapaProvincial().isBlank()
                ? datosNuevos.getChapaProvincial().trim() : null);
        existente.setTipoVehiculo(tipoNormalizado);
        existente.setCapacidadPasajeros(datosNuevos.getCapacidadPasajeros());
        existente.setKilometraje(datosNuevos.getKilometraje());

        existente.setMarca(datosNuevos.getMarca());
        existente.setModelo(datosNuevos.getModelo());
        existente.setColor(datosNuevos.getColor() != null && !datosNuevos.getColor().isBlank()
                ? datosNuevos.getColor() : "#2563eb");
        existente.setCarroceria(datosNuevos.getCarroceria());
        existente.setAnio(datosNuevos.getAnio());
        existente.setModeloAnio(datosNuevos.getModeloAnio());
        existente.setPeso(datosNuevos.getPeso());

        existente.setMarcaChasis(datosNuevos.getMarcaChasis());
        existente.setNumeroChasis(datosNuevos.getNumeroChasis());
        existente.setMarcaMotor(datosNuevos.getMarcaMotor());
        existente.setNroMotor(datosNuevos.getNroMotor());

        existente.setProcedencia(datosNuevos.getProcedencia());
        existente.setCodigoAutomotor(datosNuevos.getCodigoAutomotor());
        existente.setFechaAdquisicion(datosNuevos.getFechaAdquisicion());
        existente.setFechaInscripcion(datosNuevos.getFechaInscripcion());

        if (datosNuevos.getMatafuegos() != null) {
            existente.getMatafuegos().clear();
            for (Matafuego matafuego : datosNuevos.getMatafuegos()) {
                existente.addMatafuego(matafuego);
            }
        }
        validarMatafuegos(existente.getTipoVehiculo(), existente.getMatafuegos());

        return vehiculoRepository.save(existente);
    }

    @Transactional
    public Vehiculo actualizarMatafuegos(Long vehiculoId, List<MatafuegoDTO> dtos) {
        Vehiculo vehiculo = buscarPorId(vehiculoId);
        List<Matafuego> nuevos = new ArrayList<>();

        if (dtos != null) {
            for (MatafuegoDTO dto : dtos) {
                Matafuego matafuego = Matafuego.builder()
                        .ubicacion(dto.getUbicacion() != null && !dto.getUbicacion().isBlank()
                                ? dto.getUbicacion().trim()
                                : null)
                        .numeroSerie(dto.getNumeroSerie() != null && !dto.getNumeroSerie().isBlank()
                                ? dto.getNumeroSerie().trim()
                                : null)
                        .fechaCarga(dto.getFechaCarga())
                        .fechaVencimiento(dto.getFechaVencimiento())
                        .observaciones(dto.getObservaciones() != null && !dto.getObservaciones().isBlank()
                                ? dto.getObservaciones().trim()
                                : null)
                        .build();
                nuevos.add(matafuego);
            }
        }

        validarMatafuegos(vehiculo.getTipoVehiculo(), nuevos);

        vehiculo.getMatafuegos().clear();
        for (Matafuego matafuego : nuevos) {
            vehiculo.addMatafuego(matafuego);
        }

        return vehiculoRepository.save(vehiculo);
    }

    @Transactional(readOnly = true)
    public List<MatafuegoAlertaDTO> obtenerAlertasMatafuegos(Integer diasAnticipacion) {
        int dias = (diasAnticipacion != null && diasAnticipacion >= 0)
                ? diasAnticipacion
                : DIAS_AVISO_MATAFUEGO;
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);

        return matafuegoRepository.findAlertasActivasHasta(fechaLimite).stream()
                .map(matafuego -> {
                    Vehiculo vehiculo = matafuego.getVehiculo();
                    long diasRestantes = ChronoUnit.DAYS.between(hoy, matafuego.getFechaVencimiento());
                    String estado = diasRestantes < 0 ? "VENCIDO" : "POR_VENCER";

                    return MatafuegoAlertaDTO.builder()
                            .matafuegoId(matafuego.getId())
                            .vehiculoId(vehiculo.getId())
                            .interno(vehiculo.getInterno())
                            .patente(vehiculo.getPatente())
                            .tipoVehiculo(vehiculo.getTipoVehiculo())
                            .ubicacion(matafuego.getUbicacion())
                            .numeroSerie(matafuego.getNumeroSerie())
                            .fechaVencimiento(matafuego.getFechaVencimiento())
                            .diasRestantes(diasRestantes)
                            .estado(estado)
                            .build();
                })
                .toList();
    }

    public int obtenerCantidadMatafuegosPorTipo(String tipoVehiculo) {
        if (tipoVehiculo != null && tipoVehiculo.toLowerCase().contains("doble piso")) {
            return 2;
        }
        return 1;
    }

    private String normalizarYValidarTipoVehiculo(String tipoVehiculo) {
        if (tipoVehiculo == null || tipoVehiculo.isBlank()) {
            throw new IllegalArgumentException("El tipo de colectivo es obligatorio.");
        }
        String normalizado = tipoVehiculo.trim();
        if ("Doble piso".equalsIgnoreCase(normalizado)) {
            return "Doble piso";
        }
        if ("Piso elevado".equalsIgnoreCase(normalizado)) {
            return "Piso elevado";
        }
        throw new IllegalArgumentException(
                "Tipo de colectivo inválido. Solo se permite 'Doble piso' o 'Piso elevado'."
        );
    }

    private void validarMatafuegos(String tipoVehiculo, List<Matafuego> matafuegos) {
        if (matafuegos == null) {
            return;
        }

        int cantidadMaxima = obtenerCantidadMatafuegosPorTipo(tipoVehiculo);
        if (matafuegos.size() > cantidadMaxima) {
            throw new IllegalArgumentException(
                    "Un colectivo de tipo '" + (tipoVehiculo != null ? tipoVehiculo : "Piso Elevado")
                            + "' solo puede tener hasta " + cantidadMaxima + " matafuego(s)."
            );
        }

        for (int i = 0; i < matafuegos.size(); i++) {
            Matafuego matafuego = matafuegos.get(i);
            if (matafuego.getFechaVencimiento() == null) {
                throw new IllegalArgumentException(
                        "La fecha de vencimiento del matafuego #" + (i + 1) + " es obligatoria."
                );
            }
            if (matafuego.getFechaCarga() != null
                    && matafuego.getFechaVencimiento().isBefore(matafuego.getFechaCarga())) {
                throw new IllegalArgumentException(
                        "La fecha de vencimiento del matafuego #" + (i + 1)
                                + " no puede ser anterior a su fecha de carga."
                );
            }
            if (matafuego.getUbicacion() == null || matafuego.getUbicacion().isBlank()) {
                matafuego.setUbicacion(cantidadMaxima == 2
                        ? (i == 0 ? "Piso Inferior" : "Piso Superior")
                        : "Cabina / Piso Único");
            }
        }
    }

    // NUEVO: Dar de baja al Vehículo
    @Transactional
    public Vehiculo darDeBaja(Long id, String motivo) {
        Vehiculo vehiculo = buscarPorId(id);
        vehiculo.setActivo(false);
        vehiculo.setEstado("FUERA_DE_SERVICIO");
        vehiculo.setMotivoBaja(motivo != null && !motivo.isBlank() ? motivo.trim() : "Baja operativa general");
        vehiculo.setFechaBaja(LocalDate.now());
        return vehiculoRepository.save(vehiculo);
    }

    // NUEVO: Reactivar / Dar de alta
    @Transactional
    public Vehiculo darDeAlta(Long id) {
        Vehiculo vehiculo = buscarPorId(id);
        vehiculo.setActivo(true);
        vehiculo.setEstado("DISPONIBLE");
        vehiculo.setMotivoBaja(null);
        vehiculo.setFechaBaja(null);
        return vehiculoRepository.save(vehiculo);
    }

    private void validarReglasDeNegocio(Vehiculo vehiculo, Long idExcluido) {
        LocalDate fechaHoy = LocalDate.now();
        if (vehiculo.getFechaAdquisicion() != null && vehiculo.getFechaAdquisicion().isAfter(fechaHoy)) {
            throw new IllegalArgumentException("La fecha de adquisición no puede ser posterior a hoy.");
        }
        if (vehiculo.getFechaInscripcion() != null && vehiculo.getFechaInscripcion().isAfter(fechaHoy)) {
            throw new IllegalArgumentException("La fecha de inscripción no puede ser posterior a hoy.");
        }
        int anioActual = LocalDate.now().getYear();
        if (vehiculo.getAnio() != null && vehiculo.getAnio() > anioActual) {
            throw new IllegalArgumentException("El año de fabricación no puede ser mayor que " + anioActual + ".");
        }
        if (vehiculo.getModeloAnio() != null && vehiculo.getModeloAnio() > anioActual) {
            throw new IllegalArgumentException("El año modelo no puede ser mayor que " + anioActual + ".");
        }
    }
}