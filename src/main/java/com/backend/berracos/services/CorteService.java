
package com.backend.berracos.services;

import com.backend.berracos.models.Corte;
import com.backend.berracos.repositories.CorteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CorteService {

    private final CorteRepository corteRepository;
    private final ImagenService imagenService;

    public CorteService(
            CorteRepository corteRepository,
            ImagenService imagenService) {

        this.corteRepository = corteRepository;
        this.imagenService = imagenService;
    }

    public Corte registrarCorte(Corte corte) {

        if (corte.getNombre() == null || corte.getNombre().isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }

        if (corte.getDescripcion() == null || corte.getDescripcion().isBlank()) {
            throw new RuntimeException("La descripción es obligatoria");
        }

        if (corte.getPrecio() == null || corte.getPrecio().signum() <= 0) {
            throw new RuntimeException("El precio debe ser mayor que cero");
        }

        if (corteRepository.existsByNombre(corte.getNombre())) {
            throw new RuntimeException("El corte ya está registrado");
        }

        corte.setIdCorte(null);
        corte.setActivo(true);

        return corteRepository.save(corte);
    }

    public List<Corte> obtenerCortes() {
        return corteRepository.findByActivoTrue();
    }

    public Corte obtenerCorte(Long id) {
        return corteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corte no encontrado"));
    }

    public Corte actualizarCorte(Long id, Corte datos) {

        Corte corte = obtenerCorte(id);

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }

        if (datos.getDescripcion() == null || datos.getDescripcion().isBlank()) {
            throw new RuntimeException("La descripción es obligatoria");
        }

        if (datos.getPrecio() == null || datos.getPrecio().signum() <= 0) {
            throw new RuntimeException("El precio debe ser mayor que cero");
        }

        boolean nombreExiste = corteRepository.existsByNombre(datos.getNombre());

        if (!corte.getNombre().equalsIgnoreCase(datos.getNombre()) && nombreExiste) {
            throw new RuntimeException("Ya existe otro corte con ese nombre");
        }

        corte.setNombre(datos.getNombre());
        corte.setDescripcion(datos.getDescripcion());
        corte.setPrecio(datos.getPrecio());

        return corteRepository.save(corte);
    }

    public Corte cambiarEstado(Long id, Boolean activo) {

        if (activo == null) {
            throw new RuntimeException("Debes indicar el estado");
        }

        Corte corte = obtenerCorte(id);
        corte.setActivo(activo);

        return corteRepository.save(corte);
    }

    public Corte guardarImagen(Long id, String rutaImagen) {

        Corte corte = obtenerCorte(id);

        if (!Objects.equals(corte.getImagen(), rutaImagen)) {
            imagenService.eliminarImagen(corte.getImagen());
        }

        corte.setImagen(rutaImagen);

        return corteRepository.save(corte);
    }
}
