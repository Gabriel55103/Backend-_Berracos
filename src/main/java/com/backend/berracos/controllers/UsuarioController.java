package com.backend.berracos.controllers;

import com.backend.berracos.models.Usuario;
import com.backend.berracos.security.JwtService;
import com.backend.berracos.services.UsuarioService;
import com.backend.berracos.services.ImagenService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final ImagenService imagenService;

    public UsuarioController(
            UsuarioService usuarioService,
            JwtService jwtService,
            ImagenService imagenService) {

        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.imagenService = imagenService;
    }

    // REGISTRAR CLIENTE
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarCliente(usuario);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(respuestaUsuario(nuevoUsuario));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // INICIAR SESIÓN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos) {
        try {
            String correo = datos.get("correo");
            String password = datos.get("password");

            if (correo == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "Ingresa correo y contraseña"));
            }

            Usuario usuario = usuarioService.iniciarSesion(correo, password);

            String token = jwtService.generarToken(usuario);

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put("mensaje", "Inicio de sesión correcto");
            respuesta.put("token", token);
            respuesta.put("tipo", "Bearer");
            respuesta.put("idUsuario", usuario.getIdUsuario());
            respuesta.put("nombre", usuario.getNombre());
            respuesta.put("correo", usuario.getCorreo());
            respuesta.put("rol", usuario.getRol());

            return ResponseEntity.ok(respuesta);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // OBTENER PERFIL DEL USUARIO AUTENTICADO
    @GetMapping("/me")
    public ResponseEntity<?> obtenerPerfil(
            org.springframework.security.core.Authentication authentication) {

        Usuario usuario = usuarioService.obtenerPorCorreo(authentication.getName());

        return ResponseEntity.ok(respuestaUsuario(usuario));
    }

    // OBTENER TODOS LOS USUARIOS (ADMIN)
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuarios());
    }

    // OBTENER BARBEROS ACTIVOS
    @GetMapping("/barberos")
    public ResponseEntity<List<Map<String, Object>>> obtenerBarberosActivos() {

        List<Map<String, Object>> barberos = usuarioService
                .obtenerBarberosActivos()
                .stream()
                .map(this::respuestaUsuario)
                .toList();

        return ResponseEntity.ok(barberos);
    }

    // REGISTRAR BARBERO
    @PostMapping("/barberos")
    public ResponseEntity<?> registrarBarbero(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoBarbero = usuarioService.registrarBarbero(usuario);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(respuestaUsuario(nuevoBarbero));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // SUBIR IMAGEN DE BARBERO
    @PostMapping("/barberos/{id}/imagen")
    public ResponseEntity<?> subirImagenBarbero(
            @PathVariable Long id,
            @RequestParam("imagen") MultipartFile imagen) {

        String ruta = null;

        try {
            ruta = imagenService.guardarImagen(imagen, "barberos");

            Usuario barbero = usuarioService.guardarImagenBarbero(id, ruta);

            return ResponseEntity.ok(respuestaUsuario(barbero));

        } catch (RuntimeException e) {

            if (ruta != null) {
                imagenService.eliminarImagen(ruta);
            }

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // EDITAR CLIENTE O BARBERO
    @PutMapping("/{id}")
    public ResponseEntity<?> editarUsuario(
            @PathVariable Long id,
            @RequestBody Map<String, String> datos) {

        try {
            Usuario usuario = usuarioService.editarUsuario(
                    id,
                    datos.get("nombre"),
                    datos.get("correo"),
                    datos.get("password")
            );

            return ResponseEntity.ok(respuestaUsuario(usuario));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // ACTIVAR O DESACTIVAR CLIENTE O BARBERO
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam Boolean activo) {

        try {
            Usuario usuario = usuarioService.cambiarEstadoUsuario(id, activo);

            return ResponseEntity.ok(respuestaUsuario(usuario));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // ELIMINAR CLIENTE O BARBERO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);

            return ResponseEntity.ok(
                    Map.of("mensaje", "Usuario eliminado correctamente")
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // RESPUESTA SEGURA DEL USUARIO
    private Map<String, Object> respuestaUsuario(Usuario usuario) {

        Map<String, Object> respuesta = new HashMap<>();

        respuesta.put("idUsuario", usuario.getIdUsuario());
        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("correo", usuario.getCorreo());
        respuesta.put("rol", usuario.getRol());
        respuesta.put("activo", usuario.getActivo());
        respuesta.put("imagen", usuario.getImagen());

        return respuesta;
    }
}