package com.drivepsp.drivepsp_backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio de almacenamiento en disco. Se encarga de guardar, leer y borrar
 * los archivos cifrados en el sistema de ficheros del servidor. Cada archivo
 * se guarda en una carpeta con el ID del usuario para mantenerlos organizados.
 */
@Service
public class StorageService {

    private final Path rutaBase;

    public StorageService(@Value("${drivepsp.storage.base-path}") String basePath) {
        this.rutaBase = Paths.get(basePath);
    }

    /**
     * Guarda los bytes del archivo cifrado en disco. La ruta final sera:
     * {base-path}/{userId}/{fileId}
     *
     * @param userId id del propietario
     * @param fileId id del archivo
     * @param datos  bytes cifrados a guardar
     * @return la ruta relativa donde se ha guardado el archivo
     */
    public String guardar(UUID userId, UUID fileId, byte[] datos) {
        try {
            Path carpetaUsuario = rutaBase.resolve(userId.toString());
            Files.createDirectories(carpetaUsuario);

            Path rutaArchivo = carpetaUsuario.resolve(fileId.toString());
            Files.write(rutaArchivo, datos);

            return userId + "/" + fileId;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo en disco", e);
        }
    }

    /**
     * Lee los bytes de un archivo cifrado desde disco.
     *
     * @param rutaRelativa ruta relativa del archivo (userId/fileId)
     * @return los bytes del archivo cifrado
     */
    public byte[] leer(String rutaRelativa) {
        try {
            Path rutaArchivo = rutaBase.resolve(rutaRelativa);
            return Files.readAllBytes(rutaArchivo);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de disco", e);
        }
    }

    /**
     * Borra un archivo cifrado del disco.
     *
     * @param rutaRelativa ruta relativa del archivo (userId/fileId)
     */
    public void borrar(String rutaRelativa) {
        try {
            Path rutaArchivo = rutaBase.resolve(rutaRelativa);
            Files.deleteIfExists(rutaArchivo);
        } catch (IOException e) {
            throw new RuntimeException("Error al borrar el archivo de disco", e);
        }
    }
}
