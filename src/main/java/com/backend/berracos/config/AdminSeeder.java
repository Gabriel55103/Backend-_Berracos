package com.backend.berracos.config;

import com.backend.berracos.models.Usuario;
import com.backend.berracos.models.enums.RoleUsuario;
import com.backend.berracos.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.nombre}")
    private String nombre;

    @Value("${app.admin.correo}")
    private String correo;

    @Value("${app.admin.password}")
    private String password;

    public AdminSeeder(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (usuarioRepository.existsByCorreo(correo)) {
            System.out.println("El administrador ya existe.");
            return;
        }

        Usuario admin = new Usuario();

        admin.setNombre(nombre);
        admin.setCorreo(correo);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRol(RoleUsuario.ADMIN);
        admin.setFechaRegistro(LocalDateTime.now());
        admin.setActivo(true);

        usuarioRepository.save(admin);

        System.out.println("Administrador creado correctamente.");
        System.out.println("Correo: " + correo);
    }
}