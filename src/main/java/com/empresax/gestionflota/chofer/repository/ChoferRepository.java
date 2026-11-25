package com.empresax.gestionflota.chofer.repository;

import com.empresax.gestionflota.chofer.model.Chofer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChoferRepository extends JpaRepository<Chofer, Long> {
    Optional<Chofer> findByDni(String dni);
    List<Chofer> findByActivoTrue();
}