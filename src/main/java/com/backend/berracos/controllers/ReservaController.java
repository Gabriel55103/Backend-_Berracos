
package com.backend.berracos.controllers;

import com.backend.berracos.models.Reserva;
import com.backend.berracos.services.ReservaService;
import com.backend.berracos.services.ReservaService.ReservaAdminDTO;
import com.backend.berracos.services.ReservaService.ReservaBarberoDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // CREAR UNA RESERVACIÓN
    @PostMapping
    public ResponseEntity<?> crearReserva(
            @RequestBody Reserva reserva,
            Authentication authentication) {

        try {
            Reserva nuevaReserva = reservaService.crearReserva(
                    authentication.getName(),
                    reserva
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(nuevaReserva);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // CONSULTAR RESERVACIONES DEL CLIENTE
    @GetMapping("/mis-reservas")
    public ResponseEntity<?> obtenerMisReservas(
            Authentication authentication) {

        try {
            List<Reserva> reservas = reservaService.obtenerMisReservas(
                    authentication.getName()
            );

            return ResponseEntity.ok(reservas);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // CONSULTAR TODAS LAS RESERVACIONES DEL ADMINISTRADOR
    @GetMapping("/admin")
    public ResponseEntity<?> obtenerTodasLasReservas(
            Authentication authentication) {

        try {
            List<ReservaAdminDTO> reservas =
                    reservaService.obtenerTodasLasReservas(
                            authentication.getName()
                    );

            return ResponseEntity.ok(reservas);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // CONSULTAR RESERVACIONES ASIGNADAS AL BARBERO
    @GetMapping("/barbero")
    public ResponseEntity<?> obtenerReservasBarbero(
            Authentication authentication) {

        try {
            List<ReservaBarberoDTO> reservas =
                    reservaService.obtenerReservasBarbero(
                            authentication.getName()
                    );

            return ResponseEntity.ok(reservas);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // ACEPTAR UNA RESERVACIÓN
    @PutMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarReserva(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            Reserva reserva = reservaService.aceptarReserva(
                    authentication.getName(),
                    id
            );

            return ResponseEntity.ok(reserva);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // CANCELAR UNA RESERVACIÓN DEL CLIENTE
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            Reserva reserva = reservaService.cancelarReserva(
                    authentication.getName(),
                    id
            );

            return ResponseEntity.ok(reserva);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // CANCELAR UNA RESERVACIÓN COMO BARBERO
    @PutMapping("/{id}/cancelar-barbero")
    public ResponseEntity<?> cancelarReservaBarbero(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            Reserva reserva = reservaService.cancelarReservaBarbero(
                    authentication.getName(),
                    id
            );

            return ResponseEntity.ok(reserva);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // REGISTRAR SI EL CLIENTE ASISTIÓ O NO
    @PutMapping("/{id}/asistencia")
    public ResponseEntity<?> registrarAsistencia(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> datos,
            Authentication authentication) {

        try {
            Boolean asistio = datos.get("asistio");

            Reserva reserva = reservaService.registrarAsistencia(
                    authentication.getName(),
                    id,
                    asistio
            );

            return ResponseEntity.ok(reserva);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // CONSULTAR HORARIOS OCUPADOS
    @GetMapping("/disponibilidad")
    public ResponseEntity<?> obtenerDisponibilidad(
            @RequestParam Long idBarbero,
            @RequestParam LocalDate fecha) {

        try {
            List<LocalTime> horariosOcupados =
                    reservaService.obtenerHorariosOcupados(
                            idBarbero,
                            fecha
                    );

            return ResponseEntity.ok(horariosOcupados);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
