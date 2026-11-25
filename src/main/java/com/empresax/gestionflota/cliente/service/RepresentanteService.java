package com.empresax.gestionflota.cliente.service;

import com.empresax.gestionflota.cliente.dto.RepresentanteAltaDTO;
import com.empresax.gestionflota.cliente.model.Representante;
import com.empresax.gestionflota.cliente.repository.RepresentanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepresentanteService {

    private final RepresentanteRepository representanteRepository;

    public RepresentanteService(RepresentanteRepository representanteRepository) {
        this.representanteRepository = representanteRepository;
    }

    @Transactional(readOnly = true)
    public List<Representante> listarTodos() {
        return representanteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Representante buscarPorId(Long id) {
        return representanteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el representante con ID: " + id));
    }

    @Transactional
    public Representante registrar(RepresentanteAltaDTO dto) {
        String dni = normalizar(dto.getDni());
        validarDni(dni, null);

        Representante representante = Representante.builder()
                .email(normalizarOpcional(dto.getEmail()))
                .telefono(dto.getTelefono())
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .dni(dni)
                .build();

        return representanteRepository.save(representante);
    }

    @Transactional
    public Representante actualizar(Long id, RepresentanteAltaDTO dto) {
        Representante representante = buscarPorId(id);
        String dni = normalizar(dto.getDni());
        validarDni(dni, id);

        representante.setEmail(normalizarOpcional(dto.getEmail()));
        representante.setTelefono(dto.getTelefono());
        representante.setNombre(dto.getNombre().trim());
        representante.setApellido(dto.getApellido().trim());
        representante.setDni(dni);

        return representanteRepository.save(representante);
    }

    private void validarDni(String dni, Long id) {
        boolean existe = id == null
                ? representanteRepository.existsByDni(dni)
                : representanteRepository.existsByDniAndIdNot(dni, id);
        if (existe) {
            throw new IllegalArgumentException("Ya existe un representante con el DNI: " + dni);
        }
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }
        return valor.trim();
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
