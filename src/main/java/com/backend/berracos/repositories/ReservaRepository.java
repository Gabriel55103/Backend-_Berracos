
package com.backend.berracos.repositories;

import com.backend.berracos.models.Reserva;
import com.backend.berracos.models.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // CONTAR RESERVACIONES ACTIVAS DE UN CLIENTE
    long countByUsuario_IdUsuarioAndEstadoIn(
            Long idUsuario,
            List<EstadoReserva> estados
    );

    // CONTAR TODAS LAS RESERVACIONES DE UN CLIENTE
    long countByUsuario_IdUsuario(Long idUsuario);

    // CONTAR TODAS LAS RESERVACIONES ASIGNADAS A UN BARBERO
    long countByBarberoUsuario_IdUsuario(Long idBarbero);

    // CONSULTAR RESERVACIONES DEL CLIENTE
    List<Reserva> findByUsuario_IdUsuarioOrderByFechaDescHoraDesc(
            Long idUsuario
    );

    Optional<Reserva> findByIdReservaAndUsuario_IdUsuario(
            Long idReserva,
            Long idUsuario
    );

    // VERIFICAR SI UN HORARIO ESTÁ OCUPADO
    boolean existsByBarberoUsuario_IdUsuarioAndFechaAndHoraAndEstadoIn(
            Long idBarbero,
            LocalDate fecha,
            java.time.LocalTime hora,
            List<EstadoReserva> estados
    );

    // CONSULTAR HORARIOS OCUPADOS DE UN BARBERO
    List<Reserva> findByBarberoUsuario_IdUsuarioAndFechaAndEstadoIn(
            Long idBarbero,
            LocalDate fecha,
            List<EstadoReserva> estados
    );
}
