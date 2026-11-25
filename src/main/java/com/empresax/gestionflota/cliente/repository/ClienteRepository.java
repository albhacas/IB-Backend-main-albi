package com.empresax.gestionflota.cliente.repository;

import com.empresax.gestionflota.cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCuit(String cuit);
    boolean existsByCuitAndIdNot(String cuit, Long id);
}
