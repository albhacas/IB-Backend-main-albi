package com.empresax.gestionflota.cliente.repository;

import com.empresax.gestionflota.cliente.model.Representante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepresentanteRepository extends JpaRepository<Representante, Long> {
    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);
}
