package com.drivepsp.drivepsp_backend.controller;

import com.drivepsp.drivepsp_backend.dto.FileResponse;
import com.drivepsp.drivepsp_backend.entity.FileEntity;
import com.drivepsp.drivepsp_backend.service.FileService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador de archivos con cuatro endpoints: subir, listar, descargar y eliminar.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> upload(@RequestParam("file") MultipartFile file,
                                               Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        FileResponse response = fileService.upload(file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> list(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<FileResponse> archivos = fileService.listFiles(userId);
        return ResponseEntity.ok(archivos);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id,
                                           Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        // Obtenemos los metadatos para el nombre y tipo MIME
        FileEntity metadatos = fileService.obtenerMetadatos(id, userId);
        byte[] datos = fileService.download(id, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        metadatos.getMimeType() != null ? metadatos.getMimeType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadatos.getName() + "\"")
                .body(datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        fileService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
