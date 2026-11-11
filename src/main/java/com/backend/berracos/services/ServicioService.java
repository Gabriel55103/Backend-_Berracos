
package com.backend.berracos.services;

import com.backend.berracos.models.Servicio;
import com.backend.berracos.repositories.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    // REGISTRAR SERVICIO
    public Servicio registrarServicio(Servicio servicio) {

        validarDatos(servicio);

        if (servicioRepository.existsByNombre(servicio.getNombre())) {
            throw new RuntimeException("El servicio ya está registrado");
        }

        servicio.setIdServicio(null);
        servicio.setActivo(true);

        return servicioRepository.save(servicio);
    }

    // OBTENER SERVICIOS ACTIVOS
    public List<Servicio> obtenerServicios() {
        return servicioRepository.findByActivoTrue();
    }

    // OBTENER SERVICIO POR ID
    public Servicio obtenerServicio(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Servicio no encontrado"));
    }

    // ACTUALIZAR SERVICIO
    public Servicio actualizarServicio(Long id, Servicio datos) {

        Servicio servicio = obtenerServicio(id);

        validarDatos(datos);

        boolean nombreExiste =
                servicioRepository.existsByNombre(datos.getNombre());

        if (!servicio.getNombre().equalsIgnoreCase(datos.getNombre())
                && nombreExiste) {

            throw new RuntimeException(
                    "Ya existe otro servicio con ese nombre"
            );
        }

        servicio.setNombre(datos.getNombre());
        servicio.setDescripcion(datos.getDescripcion());
        servicio.setIcono(datos.getIcono());
        servicio.setPrecio(datos.getPrecio());

        // ACTUALIZAR IMAGEN SOLO SI SE ENVÍA UNA NUEVA
        if (datos.getImagen() != null && !datos.getImagen().isBlank()) {
            servicio.setImagen(datos.getImagen());
        }

        return servicioRepository.save(servicio);
    }

    // CAMBIAR ESTADO
    public Servicio cambiarEstado(Long id, Boolean activo) {

        if (activo == null) {
            throw new RuntimeException("Debes indicar el estado");
        }

        Servicio servicio = obtenerServicio(id);
        servicio.setActivo(activo);

        return servicioRepository.save(servicio);
    }

    // VALIDAR DATOS
    private void validarDatos(Servicio servicio) {

        if (servicio.getNombre() == null ||
                servicio.getNombre().isBlank()) {

            throw new RuntimeException("El nombre es obligatorio");
        }

        if (servicio.getDescripcion() == null ||
                servicio.getDescripcion().isBlank()) {

            throw new RuntimeException("La descripción es obligatoria");
        }

        if (servicio.getIcono() == null ||
                servicio.getIcono().isBlank()) {

            throw new RuntimeException("El icono es obligatorio");
        }

        if (servicio.getPrecio() == null ||
                servicio.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "El precio debe ser mayor que cero"
            );
        }
    }
}
