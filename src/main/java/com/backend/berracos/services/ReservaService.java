
package com.backend.berracos.services;

import com.backend.berracos.models.Reserva;
import com.backend.berracos.models.Usuario;
import com.backend.berracos.models.enums.EstadoReserva;
import com.backend.berracos.models.enums.RoleUsuario;
import com.backend.berracos.repositories.ReservaRepository;
import com.backend.berracos.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;

    private static final int MAX_RESERVAS = 3;

    public ReservaService(
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository) {

        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public record ReservaAdminDTO(
            Long idReserva,
            String nombre,
            String correoCliente,
            String servicio,
            String tipoProducto,
            String barbero,
            LocalDate fecha,
            LocalTime hora,
            Object precio,
            String estado,
            Boolean asistio
    ) {}

    public record ReservaBarberoDTO(
            Long idReserva,
            String nombre,
            String correoCliente,
            String servicio,
            String tipoProducto,
            String barbero,
            LocalDate fecha,
            LocalTime hora,
            Object precio,
            String estado,
            Boolean asistio
    ) {}

    // CREAR RESERVACION
    @Transactional
    public Reserva crearReserva(String correo, Reserva reserva) {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() != RoleUsuario.CLIENTE) {
            throw new RuntimeException(
                    "Solo los clientes pueden realizar reservaciones"
            );
        }

        if (reserva.getBarberoUsuario() == null ||
                reserva.getBarberoUsuario().getIdUsuario() == null) {

            throw new RuntimeException("Debes seleccionar un barbero");
        }

        Long idBarbero = reserva.getBarberoUsuario().getIdUsuario();

        Usuario barbero = usuarioRepository.findById(idBarbero)
                .orElseThrow(() ->
                        new RuntimeException("Barbero no encontrado"));

        if (barbero.getRol() != RoleUsuario.BARBERO) {
            throw new RuntimeException(
                    "El usuario seleccionado no es un barbero"
            );
        }

        if (!Boolean.TRUE.equals(barbero.getActivo())) {
            throw new RuntimeException(
                    "El barbero seleccionado no está disponible"
            );
        }

        if (reserva.getServicio() == null ||
                reserva.getServicio().isBlank()) {

            throw new RuntimeException(
                    "Debes seleccionar un servicio o corte"
            );
        }

        if (reserva.getTipoProducto() == null) {
            throw new RuntimeException(
                    "Debes seleccionar el tipo de producto"
            );
        }

        if (reserva.getFecha() == null ||
                reserva.getHora() == null) {

            throw new RuntimeException(
                    "Debes seleccionar una fecha y hora"
            );
        }

        if (reserva.getPrecio() == null ||
                reserva.getPrecio().signum() <= 0) {

            throw new RuntimeException(
                    "El precio de la reservación no es válido"
            );
        }

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (reserva.getFecha().isBefore(hoy) ||
                (reserva.getFecha().isEqual(hoy) &&
                        !reserva.getHora().isAfter(ahora))) {

            throw new RuntimeException(
                    "No puedes reservar una fecha u horario pasado"
            );
        }

        List<EstadoReserva> estadosActivos = List.of(
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA
        );

        long totalReservas =
                reservaRepository.countByUsuario_IdUsuarioAndEstadoIn(
                        usuario.getIdUsuario(),
                        estadosActivos
                );

        if (totalReservas >= MAX_RESERVAS) {
            throw new RuntimeException(
                    "Solo puedes tener un máximo de 3 reservaciones activas"
            );
        }

        boolean horarioOcupado =
                reservaRepository
                        .existsByBarberoUsuario_IdUsuarioAndFechaAndHoraAndEstadoIn(
                                barbero.getIdUsuario(),
                                reserva.getFecha(),
                                reserva.getHora(),
                                estadosActivos
                        );

        if (horarioOcupado) {
            throw new RuntimeException(
                    "Este horario ya está reservado. Selecciona otro."
            );
        }

        reserva.setIdReserva(null);
        reserva.setUsuario(usuario);
        reserva.setBarberoUsuario(barbero);
        reserva.setBarbero(barbero.getNombre());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setAsistio(null);

        return reservaRepository.save(reserva);
    }

    // CONSULTAR RESERVACIONES DEL CLIENTE
    @Transactional(readOnly = true)
    public List<Reserva> obtenerMisReservas(String correo) {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        return reservaRepository
                .findByUsuario_IdUsuarioOrderByFechaDescHoraDesc(
                        usuario.getIdUsuario()
                );
    }

    // CONSULTAR TODAS LAS RESERVACIONES PARA ADMINISTRADOR
    @Transactional(readOnly = true)
    public List<ReservaAdminDTO> obtenerTodasLasReservas(String correo) {

        Usuario administrador = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (administrador.getRol() != RoleUsuario.ADMIN) {
            throw new RuntimeException(
                    "No tienes permisos para consultar todas las reservaciones"
            );
        }

        return reservaRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    int comparacionFecha = b.getFecha().compareTo(a.getFecha());

                    if (comparacionFecha != 0) {
                        return comparacionFecha;
                    }

                    return b.getHora().compareTo(a.getHora());
                })
                .map(reserva -> new ReservaAdminDTO(
                        reserva.getIdReserva(),
                        reserva.getUsuario().getNombre(),
                        reserva.getUsuario().getCorreo(),
                        reserva.getServicio(),
                        reserva.getTipoProducto() != null
                                ? reserva.getTipoProducto().name()
                                : "SERVICIO",
                        reserva.getBarbero(),
                        reserva.getFecha(),
                        reserva.getHora(),
                        reserva.getPrecio(),
                        reserva.getEstado().name(),
                        reserva.getAsistio()
                ))
                .toList();
    }

    // CONSULTAR RESERVACIONES ASIGNADAS AL BARBERO
    @Transactional(readOnly = true)
    public List<ReservaBarberoDTO> obtenerReservasBarbero(String correo) {

        Usuario barbero = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (barbero.getRol() != RoleUsuario.BARBERO) {
            throw new RuntimeException(
                    "Solo los barberos pueden consultar estas reservaciones"
            );
        }

        return reservaRepository.findAll()
                .stream()
                .filter(reserva ->
                        reserva.getBarberoUsuario() != null &&
                                reserva.getBarberoUsuario().getIdUsuario()
                                        .equals(barbero.getIdUsuario())
                )
                .sorted((a, b) -> {
                    int comparacionFecha = b.getFecha().compareTo(a.getFecha());

                    if (comparacionFecha != 0) {
                        return comparacionFecha;
                    }

                    return b.getHora().compareTo(a.getHora());
                })
                .map(reserva -> new ReservaBarberoDTO(
                        reserva.getIdReserva(),
                        reserva.getUsuario().getNombre(),
                        reserva.getUsuario().getCorreo(),
                        reserva.getServicio(),
                        reserva.getTipoProducto() != null
                                ? reserva.getTipoProducto().name()
                                : "SERVICIO",
                        reserva.getBarbero(),
                        reserva.getFecha(),
                        reserva.getHora(),
                        reserva.getPrecio(),
                        reserva.getEstado().name(),
                        reserva.getAsistio()
                ))
                .toList();
    }

    // CONFIRMAR RESERVACION ASIGNADA AL BARBERO
    @Transactional
    public Reserva aceptarReserva(String correo, Long idReserva) {

        Usuario barbero = obtenerBarberoAutenticado(correo);

        Reserva reserva = obtenerReservaDelBarbero(
                idReserva,
                barbero
        );

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new RuntimeException(
                    "Solamente puedes confirmar reservaciones pendientes"
            );
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setAsistio(null);

        return reservaRepository.save(reserva);
    }

    // CANCELAR RESERVACION POR PARTE DEL BARBERO
    @Transactional
    public Reserva cancelarReservaBarbero(String correo, Long idReserva) {

        Usuario barbero = obtenerBarberoAutenticado(correo);

        Reserva reserva = obtenerReservaDelBarbero(
                idReserva,
                barbero
        );

        if (reserva.getEstado() != EstadoReserva.PENDIENTE &&
                reserva.getEstado() != EstadoReserva.CONFIRMADA) {

            throw new RuntimeException(
                    "Solamente puedes cancelar reservaciones pendientes o confirmadas"
            );
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setAsistio(null);

        return reservaRepository.save(reserva);
    }

    // REGISTRAR ASISTENCIA DEL CLIENTE
    @Transactional
    public Reserva registrarAsistencia(
            String correo,
            Long idReserva,
            Boolean asistio) {

        if (asistio == null) {
            throw new RuntimeException(
                    "Debes indicar si el cliente acudió o no"
            );
        }

        Usuario barbero = obtenerBarberoAutenticado(correo);

        Reserva reserva = obtenerReservaDelBarbero(
                idReserva,
                barbero
        );

        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new RuntimeException(
                    "Solo puedes registrar asistencia de citas confirmadas"
            );
        }

        if (reserva.getFecha().isAfter(LocalDate.now())) {
            throw new RuntimeException(
                    "No puedes registrar asistencia de una cita futura"
            );
        }

        if (reserva.getFecha().isEqual(LocalDate.now()) &&
                reserva.getHora().isAfter(LocalTime.now())) {

            throw new RuntimeException(
                    "Todavía no llega la hora de esta reservación"
            );
        }

        reserva.setAsistio(asistio);
        reserva.setEstado(EstadoReserva.COMPLETADA);

        return reservaRepository.save(reserva);
    }

    // CANCELAR RESERVACION DEL CLIENTE
    @Transactional
    public Reserva cancelarReserva(String correo, Long idReserva) {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        Reserva reserva = reservaRepository
                .findByIdReservaAndUsuario_IdUsuario(
                        idReserva,
                        usuario.getIdUsuario()
                )
                .orElseThrow(() ->
                        new RuntimeException("Reservación no encontrada"));

        if (reserva.getEstado() == EstadoReserva.CANCELADA ||
                reserva.getEstado() == EstadoReserva.COMPLETADA) {

            throw new RuntimeException(
                    "Esta reservación ya no se puede cancelar"
            );
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setAsistio(null);

        return reservaRepository.save(reserva);
    }

    // CONSULTAR HORARIOS OCUPADOS
    public List<LocalTime> obtenerHorariosOcupados(
            Long idBarbero,
            LocalDate fecha) {

        Usuario barbero = usuarioRepository.findById(idBarbero)
                .orElseThrow(() ->
                        new RuntimeException("Barbero no encontrado"));

        if (barbero.getRol() != RoleUsuario.BARBERO ||
                !Boolean.TRUE.equals(barbero.getActivo())) {

            throw new RuntimeException("Barbero no disponible");
        }

        List<EstadoReserva> estadosOcupados = List.of(
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA
        );

        return reservaRepository
                .findByBarberoUsuario_IdUsuarioAndFechaAndEstadoIn(
                        idBarbero,
                        fecha,
                        estadosOcupados
                )
                .stream()
                .map(Reserva::getHora)
                .toList();
    }

    // VALIDAR BARBERO AUTENTICADO
    private Usuario obtenerBarberoAutenticado(String correo) {

        Usuario barbero = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (barbero.getRol() != RoleUsuario.BARBERO) {
            throw new RuntimeException(
                    "Esta operación solamente está permitida para barberos"
            );
        }

        return barbero;
    }

    // VALIDAR QUE LA RESERVACION PERTENEZCA AL BARBERO
    private Reserva obtenerReservaDelBarbero(
            Long idReserva,
            Usuario barbero) {

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() ->
                        new RuntimeException("Reservación no encontrada"));

        if (reserva.getBarberoUsuario() == null ||
                !reserva.getBarberoUsuario().getIdUsuario()
                        .equals(barbero.getIdUsuario())) {

            throw new RuntimeException(
                    "Esta reservación no está asignada a tu usuario"
            );
        }

        return reserva;
    }
}
