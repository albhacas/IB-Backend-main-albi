package com.empresax.gestionflota.vehiculo.repository;

import com.empresax.gestionflota.vehiculo.model.Matafuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatafuegoRepository extends JpaRepository<Matafuego, Long> {

    @Query("""
           SELECT m FROM Matafuego m
           JOIN FETCH m.vehiculo v
           WHERE v.activo = true AND m.fechaVencimiento <= :fechaLimite
           ORDER BY m.fechaVencimiento ASC
           """)
    List<Matafuego> findAlertasActivasHasta(@Param("fechaLimite") LocalDate fechaLimite);
}