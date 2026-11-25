package com.empresax.gestionflota.vehiculo.service;

import com.empresax.gestionflota.vehiculo.dto.MantenimientoAltaDTO;
import com.empresax.gestionflota.vehiculo.model.Mantenimiento;
import com.empresax.gestionflota.vehiculo.model.Vehiculo;
import com.empresax.gestionflota.vehiculo.repository.MantenimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MantenimientoService {
    private final MantenimientoRepository mantenimientoRepository;
    private final VehiculoService vehiculoService;

    public MantenimientoService(MantenimientoRepository mantenimientoRepository,
                                VehiculoService vehiculoService) {
        this.mantenimientoRepository = mantenimientoRepository;
        this.vehiculoService = vehiculoService;
    }

    @Transactional(readOnly = true)
    public List<Mantenimiento> listarTodos() {
        return mantenimientoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Mantenimiento buscarPorId(Long id) {
        return mantenimientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el mantenimiento con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Mantenimiento> obtenerHistorialPorVehiculo(Long vehiculoId) {
        return mantenimientoRepository.findByVehiculoId(vehiculoId);
    }

    @Transactional
    public Mantenimiento registrarMantenimiento(MantenimientoAltaDTO dto) {
        Vehiculo vehiculo = vehiculoService.buscarPorId(dto.getVehiculoId());
        LocalDate fecha = (dto.getFechaMantenimiento() != null) ? dto.getFechaMantenimiento() : LocalDate.now();

        Mantenimiento mantenimiento = Mantenimiento.builder()
                .vehiculo(vehiculo)
                .patenteVehiculo(vehiculo != null ? vehiculo.getPatente() : "")
                .tipoServicio(dto.getTipoServicio())
                .fechaMantenimiento(fecha)
                .fechaIngreso(fecha)
                .fechaEstimadaEntrega(dto.getFechaEstimadaEntrega())
                .costo(dto.getCosto() != null ? dto.getCosto() : BigDecimal.ZERO)
                .observaciones(dto.getObservaciones())
                .build();

        return mantenimientoRepository.save(mantenimiento);
    }
}