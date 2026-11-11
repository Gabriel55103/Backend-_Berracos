
package com.backend.berracos.controllers;

import com.backend.berracos.models.Corte;
import com.backend.berracos.services.CorteService;
import com.backend.berracos.services.ImagenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/cortes")
@CrossOrigin(origins = "http://localhost:3000")
public class CorteController {

    private final CorteService corteService;
    private final ImagenService imagenService;

    public CorteController(
            CorteService corteService,
            ImagenService imagenService) {

        this.corteService = corteService;
        this.imagenService = imagenService;
    }

    @PostMapping
    public ResponseEntity<?> registrarCorte(@RequestBody Corte corte) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(corteService.registrarCorte(corte));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerCortes() {
        try {
            return ResponseEntity.ok(corteService.obtenerCortes());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCorte(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(corteService.obtenerCorte(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCorte(
            @PathVariable Long id,
            @RequestBody Corte corte) {
        try {
            return ResponseEntity.ok(
                    corteService.actualizarCorte(id, corte)
            );
        } catch (RuntimeException e) {
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
                    corteService.cambiarEstado(id, activo)
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

        try {
            String ruta = imagenService.guardarImagen(imagen, "cortes");

            return ResponseEntity.ok(
                    corteService.guardarImagen(id, ruta)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
