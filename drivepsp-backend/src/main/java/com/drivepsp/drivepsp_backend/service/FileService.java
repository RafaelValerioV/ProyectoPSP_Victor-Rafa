package com.drivepsp.drivepsp_backend.service;

import com.drivepsp.drivepsp_backend.concurrency.GestorEspacio;
import com.drivepsp.drivepsp_backend.dto.FileResponse;
import com.drivepsp.drivepsp_backend.entity.FileEntity;
import com.drivepsp.drivepsp_backend.entity.User;
import com.drivepsp.drivepsp_backend.exception.StorageFullException;
import com.drivepsp.drivepsp_backend.repository.FileRepository;
import com.drivepsp.drivepsp_backend.repository.UserRepository;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Coordina la subida, listado, descarga y eliminacion de archivos.
 *
 * En la subida se reserva espacio en los semaforos antes de cifrar y guardar
 * el archivo. Si algo falla tras la reserva, se liberan (borran) los permisos para no
 * dejar espacio bloqueado. En la eliminacion el proceso es el inverso: se
 * borra el archivo y se devuelve el espacio a los semaforos
 */
@Service
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final StorageService storageService;
    private final GestorEspacio gestorEspacio;

    public FileService(FileRepository fileRepository,
                       UserRepository userRepository,
                       EncryptionService encryptionService,
                       StorageService storageService,
                       GestorEspacio gestorEspacio) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.storageService = storageService;
        this.gestorEspacio = gestorEspacio;
    }

    /**
     * Sube un archivo: reserva espacio en los semaforos, cifra el contenido,
     * lo guarda en disco y registra los metadtos. Si no hay espacio lanza
     * StorageFullException; si falla otro paso libera los permisos reservados
     *
     * @param file archivo recibido como MultipartFile
     * @param userId id del usuario autenticado
     * @return metadatos del archivo subido
     */
    public FileResponse upload(MultipartFile file, UUID userId) {
        long tamano = file.getSize();
        int kb = GestorEspacio.bytesAKb(tamano);

        if (!gestorEspacio.reservar(userId, kb)) {
            throw new StorageFullException("No hay espacio suficiente para subir el archivo");
        }

        try {
            byte[] datosOriginales = file.getBytes();

            String checksum = calcularSha256(datosOriginales);

            // Si ya existe un archivo con ese nombre, añadimos al final "(1)"", "(2)""...
            String nombreFinal = generarNombreUnico(userId, file.getOriginalFilename());

            byte[] datosCifrados = encryptionService.cifrar(datosOriginales);

            UUID fileId = UUID.randomUUID();
            String rutaAlmacenamiento = storageService.guardar(userId, fileId, datosCifrados);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            FileEntity fileEntity = new FileEntity();
            fileEntity.setId(fileId);
            fileEntity.setName(nombreFinal);
            fileEntity.setMimeType(file.getContentType());
            fileEntity.setSizeBytes(tamano);
            fileEntity.setStoragePath(rutaAlmacenamiento);
            fileEntity.setOwner(user);
            fileEntity.setChecksumSha256(checksum);
            fileRepository.save(fileEntity);

            user.setStorageUsed(user.getStorageUsed() + tamano);
            userRepository.save(user);

            return new FileResponse(
                    fileEntity.getId(),
                    fileEntity.getName(),
                    fileEntity.getMimeType(),
                    fileEntity.getSizeBytes(),
                    fileEntity.getCreatedAt()
            );
        } catch (StorageFullException e) {
            throw e;
        } catch (Exception e) {
            // Si algo falla, devolvemos los permisos al semaforo
            gestorEspacio.liberar(userId, kb);
            throw new RuntimeException("Error al subir el archivo", e);
        }
    }

    /**
     * Devuelve los archivos del usuario ordenados por fecha de creacion descendente.
     * @param userId id del usuario autenticado
     * @return lista de metadatos de sus archivos
     */
    public List<FileResponse> listFiles(UUID userId) {
        List<FileEntity> archivos = fileRepository.findByOwnerIdOrderByCreatedAtDesc(userId);

        return archivos.stream()
                .map(f -> new FileResponse(
                        f.getId(),
                        f.getName(),
                        f.getMimeType(),
                        f.getSizeBytes(),
                        f.getCreatedAt()
                ))
                .toList();
    }

    /**
     * Descarga un archivo comprobando que pertenece al usuario, lo lee del
     * disco y lo descifra antes de devolverlo
     * @param fileId id del archivo
     * @param userId id del usuario autenticado
     * @return bytes del archivo descifrado
     */
    public byte[] download(UUID fileId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado"));

        if (!fileEntity.getOwner().getId().equals(userId)) {
            throw new SecurityException("No tienes permiso para acceder a este archivo");
        }

        // Leemos el archvo cifrado del disco y lo desciframos
        byte[] datosCifrados = storageService.leer(fileEntity.getStoragePath());
        return encryptionService.descifrar(datosCifrados);
    }

    /**
     * Obtiene los metadatos de un archivo verificando que pertenece al usuario.
     * Se usa para construir la cabecera Content-Disposition en la descarga
     *
     * @param fileId id del archivo
     * @param userId id del usuario autenticado
     * @return entidad FileEntity con los metadatos
     */
    public FileEntity obtenerMetadatos(UUID fileId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado"));

        if (!fileEntity.getOwner().getId().equals(userId)) {
            throw new SecurityException("No tienes permiso para acceder a este archivo");
        }

        return fileEntity;
    }

    /**
     * Elimina un archivo verificando que pertenece al usuario, lo borra de disco
     * y de la base de datos, y libera el espacio en los semaforos.
     *
     * @param fileId id del archivo a eliminar
     * @param userId id del usuario autenticado
     */
    public void delete(UUID fileId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado"));

        if (!fileEntity.getOwner().getId().equals(userId)) {
            throw new SecurityException("No tienes permiso para eliminar este archivo");
        }

        long tamano = fileEntity.getSizeBytes();
        int kb = GestorEspacio.bytesAKb(tamano);

        storageService.borrar(fileEntity.getStoragePath());
        fileRepository.delete(fileEntity);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setStorageUsed(Math.max(0, user.getStorageUsed() - tamano));
        userRepository.save(user);

        gestorEspacio.liberar(userId, kb);
    }

    /**
     * Calcula el hash SHA-256 de un array de bytes y lo devuelve como
     * cadena hexadecimal. Lo usamos para verificar la integridad de los
     * archivos subidos.
     */
    /**
     * Genera un nombre unico para el archivo. Si el usuario ya tiene un archivo
     * con el mismo nombre, le anade un sufijo (1), (2), etc., igual que Windows
     * al copiar archivos duplicados. Por ejemplo: foto.jpg -> foto (1).jpg
     */
    private String generarNombreUnico(UUID userId, String nombreOriginal) {
        if (!fileRepository.existsByOwnerIdAndName(userId, nombreOriginal)) {
            return nombreOriginal;
        }

        String nombre;
        String extension;
        int puntoIdx = nombreOriginal.lastIndexOf('.');
        if (puntoIdx > 0) {
            nombre = nombreOriginal.substring(0, puntoIdx);
            extension = nombreOriginal.substring(puntoIdx);
        } else {
            nombre = nombreOriginal;
            extension = "";
        }

        int contador = 1;
        String candidato;
        do {
            candidato = nombre + " (" + contador + ")" + extension;
            contador++;
        } while (fileRepository.existsByOwnerIdAndName(userId, candidato));

        return candidato;
    }

    private String calcularSha256(byte[] datos) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datos);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular el checksum SHA-256", e);
        }
    }
}
