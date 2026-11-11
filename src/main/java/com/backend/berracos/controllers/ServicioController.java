
package com.backend.berracos.controllers;

import com.backend.berracos.models.Servicio;
import com.backend.berracos.services.ServicioService;
import com.backend.berracos.services.ImagenService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "http://localhost:3000")
public class ServicioController {

    private final ServicioService servicioService;
    private final ImagenService imagenService;

    public ServicioController(
            ServicioService servicioService,
            ImagenService imagenService) {
        this.servicioService = servicioService;
        this.imagenService = imagenService;
    }

    @PostMapping
    public ResponseEntity<?> registrarServicio(
            @RequestBody Servicio servicio) {

        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(servicioService.registrarServicio(servicio));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerServicios() {

        try {
            return ResponseEntity.ok(
                    servicioService.obtenerServicios()
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerServicio(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(
                    servicioService.obtenerServicio(id)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarServicio(
            @PathVariable Long id,
            @RequestBody Servicio servicio) {

        try {
            return ResponseEntity.ok(
                    servicioService.actualizarServicio(id, servicio)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> subirImagen(
            @PathVariable Long id,
            @RequestParam("imagen") MultipartFile imagen) {

        String ruta = null;

        try {
            Servicio servicio = servicioService.obtenerServicio(id);

            ruta = imagenService.guardarImagen(imagen, "servicios");

            String imagenAnterior = servicio.getImagen();

            servicio.setImagen(ruta);

            Servicio actualizado =
                    servicioService.actualizarServicio(id, servicio);

            if (imagenAnterior != null && !imagenAnterior.equals(ruta)) {
                imagenService.eliminarImagen(imagenAnterior);
            }

            return ResponseEntity.ok(actualizado);

        } catch (RuntimeException e) {

            if (ruta != null) {
                imagenService.eliminarImagen(ruta);
            }

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {

        try {
            return ResponseEntity.ok(
                    servicioService.cambiarEstado(id, activo)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
