package com.backend.berracos.repositories;

import com.backend.berracos.models.Corte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorteRepository extends JpaRepository<Corte, Long> {

    List<Corte> findByActivoTrue();

    boolean existsByNombre(String nombre);
}