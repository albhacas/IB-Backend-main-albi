package com.empresax.gestionflota.cliente.service;

import com.empresax.gestionflota.cliente.dto.ClienteAltaDTO;
import com.empresax.gestionflota.cliente.model.Cliente;
import com.empresax.gestionflota.cliente.model.Representante;
import com.empresax.gestionflota.cliente.repository.ClienteRepository;
import com.empresax.gestionflota.cliente.repository.RepresentanteRepository;
import com.empresax.gestionflota.viaje.repository.ViajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final RepresentanteRepository representanteRepository;
    private final ViajeRepository viajeRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          RepresentanteRepository representanteRepository,
                          ViajeRepository viajeRepository) {
        this.clienteRepository = clienteRepository;
        this.representanteRepository = representanteRepository;
        this.viajeRepository = viajeRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el cliente con ID: " + id));
    }

    @Transactional
    public Cliente registrar(ClienteAltaDTO dto) {
        String cuit = normalizar(dto.getCuit(), "El CUIT es obligatorio.");
        validarCuit(cuit, null);

        Cliente cliente = Cliente.builder()
                .razonSocial(normalizar(dto.getRazonSocial(), "La razón social es obligatoria."))
                .cuit(cuit)
                .direccion(normalizarOpcional(dto.getDireccion()))
                .telefono(dto.getTelefono())
                .email(normalizarOpcional(dto.getEmail()))
                .representante(buscarRepresentante(dto.getRepresentanteId()))
                .build();

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(Long id, ClienteAltaDTO dto) {
        Cliente cliente = buscarPorId(id);
        String cuit = normalizar(dto.getCuit(), "El CUIT es obligatorio.");
        validarCuit(cuit, id);

        cliente.setRazonSocial(normalizar(dto.getRazonSocial(), "La razón social es obligatoria."));
        cliente.setCuit(cuit);
        cliente.setDireccion(normalizarOpcional(dto.getDireccion()));
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(normalizarOpcional(dto.getEmail()));
        cliente.setRepresentante(buscarRepresentante(dto.getRepresentanteId()));

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscarPorId(id);
        if (viajeRepository.existsByCliente_Id(id)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el cliente porque tiene viajes asociados.");
        }

        Representante representante = cliente.getRepresentante();
        clienteRepository.delete(cliente);
        clienteRepository.flush();
        if (representante != null) {
            representanteRepository.delete(representante);
        }
    }

    private Representante buscarRepresentante(Long representanteId) {
        if (representanteId == null) {
            throw new IllegalArgumentException("El representante es obligatorio.");
        }
        return representanteRepository.findById(representanteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el representante con ID: " + representanteId));
    }

    private void validarCuit(String cuit, Long id) {
        boolean existe = id == null
                ? clienteRepository.existsByCuit(cuit)
                : clienteRepository.existsByCuitAndIdNot(cuit, id);
        if (existe) {
            throw new IllegalArgumentException("Ya existe un cliente con el CUIT: " + cuit);
        }
    }

    private String normalizar(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
