
package com.backend.berracos.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImagenService {

    @Value("${app.upload.dir:uploads}")
    private String directorio;

    private Path rutaBase;

    @PostConstruct
    public void inicializar() throws IOException {
        rutaBase = Paths.get(directorio).toAbsolutePath().normalize();

        Files.createDirectories(rutaBase.resolve("cortes"));
        Files.createDirectories(rutaBase.resolve("barberos"));
        Files.createDirectories(rutaBase.resolve("servicios"));
    }

    public String guardarImagen(MultipartFile archivo, String carpeta) {

        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Debes seleccionar una imagen");
        }

        if (!carpeta.equals("cortes")
                && !carpeta.equals("barberos")
                && !carpeta.equals("servicios")) {
            throw new RuntimeException("Carpeta de imágenes no válida");
        }

        String tipo = archivo.getContentType();

        if (tipo == null ||
                !(tipo.equals("image/jpeg") ||
                        tipo.equals("image/png") ||
                        tipo.equals("image/webp"))) {
            throw new RuntimeException(
                    "Solo se permiten imágenes JPG, PNG o WEBP"
            );
        }

        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException(
                    "La imagen no debe superar los 5 MB"
            );
        }

        String extension = switch (tipo) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new RuntimeException(
                    "Formato de imagen no válido"
            );
        };

        String nombreArchivo = UUID.randomUUID() + extension;

        try {
            Path destino = rutaBase
                    .resolve(carpeta)
                    .resolve(nombreArchivo)
                    .normalize();

            if (!destino.startsWith(rutaBase)) {
                throw new RuntimeException("Ruta de archivo no válida");
            }

            Files.copy(archivo.getInputStream(), destino);

            return "/uploads/" + carpeta + "/" + nombreArchivo;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen");
        }
    }

    public void eliminarImagen(String rutaImagen) {

        if (rutaImagen == null || rutaImagen.isBlank()) {
            return;
        }

        if (!rutaImagen.startsWith("/uploads/cortes/")
                && !rutaImagen.startsWith("/uploads/barberos/")
                && !rutaImagen.startsWith("/uploads/servicios/")) {
            return;
        }

        try {
            Path archivo = rutaBase
                    .resolve(rutaImagen.substring("/uploads/".length()))
                    .normalize();

            if (archivo.startsWith(rutaBase)) {
                Files.deleteIfExists(archivo);
            }

        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar la imagen");
        }
    }
}
