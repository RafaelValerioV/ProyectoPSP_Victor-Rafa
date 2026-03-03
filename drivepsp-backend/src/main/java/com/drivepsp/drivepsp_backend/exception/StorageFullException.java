package com.drivepsp.drivepsp_backend.exception;

/**
 * Excepcion que se lanza cuando no hay espacio suficiente para subir un archivo.
 * Puede ocurrir por dos motivos: que el servidor haya alcanzado su limite total
 * de 500 MB (recurso compartido entre todos los usuarios), o que el usuario
 * haya agotado su cuota individual de 100 MB. En ambos casos, el GestorEspacio
 * detecta la falta de espacio mediante los semaforos y el FileService lanza
 * esta excepcion, que el GlobalExceptionHandler traduce a un HTTP 507
 * (Insufficient Storage).
 */
public class StorageFullException extends RuntimeException {

    public StorageFullException(String mensaje) {
        super(mensaje);
    }
}
