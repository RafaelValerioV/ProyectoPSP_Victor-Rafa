package com.drivepsp.drivepsp_backend.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para toda la aplicacion. Con la anotacion
 * RestControllerAdvice, Spring intercepta las excepciones que se lanzan desde
 * cualquier controlador y las redirige a los metodos de esta clase. Asi
 * centralizamos el tratamiento de errores en un unico lugar y devolvemos
 * siempre respuestas JSON coherentes con el codigo HTTP adecuado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Captura la excepcion StorageFullException, que se lanza cuando el servidor
     * o el usuario no tienen espacio suficiente para subir un archivo.
     * Devolvemos un HTTP 507 (Insufficient Storage)
     */
    @ExceptionHandler(StorageFullException.class)
    public ResponseEntity<Map<String, String>> handleStorageFull(StorageFullException e) {
        return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Captura IllegalArgumentException, que usamos para errores de logica de
     * negocio como credenciales incorrectas, email duplicado o recurso no
     * encontrado. Devolvemos un HTTP 400 (Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Captura los errores de validacion de los DTOs (por ejemplo, cuando un
     * campo obligatorio viene vacio o el email no tiene formato valido).
     * Spring lanza esta excepcion automaticamente gracias a la anotacion
     * Valid en los controladores. Devolvemos un HTTP 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Error de validacion");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", mensaje));
    }

    /**
     * Captura SecurityException, que lanzamos cuando un usuario intenta
     * acceder o eliminar un archivo que no le pertenece.
     * Devolvemos un HTTP 403 (Forbidden)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurity(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Captura cualquier otra excepcion no controlada. No mostramos el mensaje
     * real del error al cliente por seguridad, solo devolvemos un mensaje
     * generico con HTTP 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        log.error("Excepcion no controlada", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
    }
}
