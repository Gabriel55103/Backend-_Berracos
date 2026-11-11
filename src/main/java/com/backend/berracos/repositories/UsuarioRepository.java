
package com.backend.berracos.repositories;

import com.backend.berracos.models.Usuario;
import com.backend.berracos.models.enums.RoleUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    // BUSCAR CORREO SIN DISTINGUIR MAYÚSCULAS Y MINÚSCULAS
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreo(String correo);

    Optional<Usuario> findByIdUsuarioAndRol(
            Long idUsuario,
            RoleUsuario rol
    );

    // OBTENER USUARIOS POR ROL Y QUE ESTÉN ACTIVOS
    List<Usuario> findByRolAndActivoTrue(RoleUsuario rol);
}
