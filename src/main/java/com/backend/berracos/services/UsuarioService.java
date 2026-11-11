
package com.backend.berracos.services;

import com.backend.berracos.models.Usuario;
import com.backend.berracos.models.enums.RoleUsuario;
import com.backend.berracos.repositories.UsuarioRepository;
import com.backend.berracos.repositories.ReservaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImagenService imagenService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ReservaRepository reservaRepository,
            PasswordEncoder passwordEncoder,
            ImagenService imagenService) {

        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.passwordEncoder = passwordEncoder;
        this.imagenService = imagenService;
    }

    // REGISTRAR CLIENTE
    public Usuario registrarCliente(Usuario usuario) {

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        usuario.setIdUsuario(null);
        usuario.setRol(RoleUsuario.CLIENTE);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    // REGISTRAR BARBERO
    public Usuario registrarBarbero(Usuario usuario) {

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        usuario.setIdUsuario(null);
        usuario.setRol(RoleUsuario.BARBERO);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    // INICIAR SESIÓN
    public Usuario iniciarSesion(String correo, String password) {

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() ->
                        new RuntimeException("Correo o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException("La cuenta está desactivada");
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Correo o contraseña incorrectos");
        }

        return usuario;
    }

    // OBTENER TODOS LOS USUARIOS
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    // OBTENER BARBEROS ACTIVOS
    public List<Usuario> obtenerBarberosActivos() {
        return usuarioRepository.findByRolAndActivoTrue(RoleUsuario.BARBERO);
    }

    // OBTENER USUARIO POR CORREO
    public Usuario obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));
    }

    // EDITAR CLIENTE O BARBERO
    public Usuario editarUsuario(
            Long id,
            String nombre,
            String correo,
            String password) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == RoleUsuario.ADMIN) {
            throw new RuntimeException(
                    "No se pueden modificar administradores desde este panel");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio");
        }

        if (correo == null || correo.trim().isEmpty()) {
            throw new RuntimeException("El correo es obligatorio");
        }

        String correoNuevo = correo.trim();

        usuarioRepository.findByCorreoIgnoreCase(correoNuevo).ifPresent(existente -> {
            if (!Objects.equals(
                    existente.getIdUsuario(),
                    usuario.getIdUsuario())) {
                throw new RuntimeException("El correo ya está registrado");
            }
        });

        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correoNuevo);

        if (password != null && !password.trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        return usuarioRepository.save(usuario);
    }

    // ACTIVAR O DESACTIVAR USUARIO
    public Usuario cambiarEstadoUsuario(Long id, Boolean activo) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == RoleUsuario.ADMIN) {
            throw new RuntimeException(
                    "No se puede modificar el estado de un administrador");
        }

        if (activo == null) {
            throw new RuntimeException("Debes indicar el estado");
        }

        usuario.setActivo(activo);

        return usuarioRepository.save(usuario);
    }

    // ELIMINAR USUARIO
    public void eliminarUsuario(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == RoleUsuario.ADMIN) {
            throw new RuntimeException(
                    "No se pueden eliminar administradores");
        }

        // VERIFICAR RESERVACIONES COMO CLIENTE
        long reservacionesCliente =
                reservaRepository.countByUsuario_IdUsuario(id);

        // VERIFICAR RESERVACIONES COMO BARBERO
        long reservacionesBarbero = 0;

        if (usuario.getRol() == RoleUsuario.BARBERO) {
            reservacionesBarbero =
                    reservaRepository.countByBarberoUsuario_IdUsuario(id);
        }

        // IMPEDIR ELIMINACIÓN SI TIENE HISTORIAL DE RESERVACIONES
        if (reservacionesCliente > 0 || reservacionesBarbero > 0) {
            throw new RuntimeException(
                    "No se puede eliminar este usuario porque tiene reservaciones asociadas. Desactiva su cuenta.");
        }

        // ELIMINAR IMAGEN SOLAMENTE DESPUÉS DE LAS VALIDACIONES
        if (usuario.getRol() == RoleUsuario.BARBERO) {
            imagenService.eliminarImagen(usuario.getImagen());
        }

        // ELIMINAR USUARIO
        usuarioRepository.delete(usuario);
    }

    // GUARDAR IMAGEN DE BARBERO
    public Usuario guardarImagenBarbero(Long id, String rutaImagen) {

        Usuario barbero = usuarioRepository
                .findByIdUsuarioAndRol(id, RoleUsuario.BARBERO)
                .orElseThrow(() ->
                        new RuntimeException("Barbero no encontrado"));

        if (!Objects.equals(barbero.getImagen(), rutaImagen)) {
            imagenService.eliminarImagen(barbero.getImagen());
        }

        barbero.setImagen(rutaImagen);

        return usuarioRepository.save(barbero);
    }
}
