
package com.backend.berracos.config;

import com.backend.berracos.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // REGISTRO E INICIO DE SESIÓN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios",
                                "/api/usuarios/login"
                        ).permitAll()

                        // CONSULTAR BARBEROS ACTIVOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios/barberos"
                        ).permitAll()

                        // SERVICIOS, CORTES E IMÁGENES PÚBLICAS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/servicios",
                                "/api/cortes",
                                "/uploads/**"
                        ).permitAll()

                        // CONSULTAR DISPONIBILIDAD
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservas/disponibilidad"
                        ).permitAll()

                        // REGISTRAR Y SUBIR IMÁGENES DE BARBEROS
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/barberos",
                                "/api/usuarios/barberos/**"
                        ).hasRole("ADMIN")

                        // CONSULTAR TODOS LOS USUARIOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios"
                        ).hasRole("ADMIN")

                        // EDITAR USUARIOS
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/usuarios/*"
                        ).hasRole("ADMIN")

                        // ACTIVAR O DESACTIVAR USUARIOS
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/usuarios/*"
                        ).hasRole("ADMIN")

                        // ELIMINAR USUARIOS
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/usuarios/*"
                        ).hasRole("ADMIN")

                        // ADMINISTRAR SERVICIOS Y CORTES
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/servicios",
                                "/api/cortes"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/servicios/**",
                                "/api/cortes/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/servicios/**",
                                "/api/cortes/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/servicios/**",
                                "/api/cortes/**"
                        ).hasRole("ADMIN")

                        // RUTAS DE ADMINISTRACIÓN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // CONSULTAR RESERVACIONES DEL BARBERO
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservas/barbero"
                        ).hasRole("BARBERO")

                        // ACEPTAR RESERVACIONES
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/reservas/*/aceptar"
                        ).hasRole("BARBERO")

                        // CANCELAR RESERVACIONES COMO BARBERO
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/reservas/*/cancelar-barbero"
                        ).hasRole("BARBERO")

                        // REGISTRAR ASISTENCIA DEL CLIENTE
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/reservas/*/asistencia"
                        ).hasRole("BARBERO")

                        // CONSULTAR TODAS LAS RESERVACIONES
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservas/admin"
                        ).hasRole("ADMIN")

                        // LAS DEMÁS RUTAS REQUIEREN AUTENTICACIÓN
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:3000")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
