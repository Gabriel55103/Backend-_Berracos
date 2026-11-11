
package com.backend.berracos.repositories;

import com.backend.berracos.models.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByActivoTrue();

    boolean existsByNombre(String nombre);
}
