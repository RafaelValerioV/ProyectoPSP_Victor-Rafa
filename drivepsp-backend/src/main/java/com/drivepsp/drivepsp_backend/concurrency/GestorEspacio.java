package com.drivepsp.drivepsp_backend.concurrency;

import com.drivepsp.drivepsp_backend.entity.User;
import com.drivepsp.drivepsp_backend.repository.FileRepository;
import com.drivepsp.drivepsp_backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Controla el espacio de almacenamiento disponible usando semaforos para evitar
 * condiciones de carrera cuando varios usuarios suben archivos a la vez.
 *
 * Hay dos semaforos: uno global para el servidor (500 MB en total) y uno por
 * cada usuario (100 MB por defecto). Cada permiso equivale a 1 KB. Al reservar
 * espacio se usa tryAcquire, que devuelve false en lugar de bloquear el hilo si
 * no hay permisos suficientes.
 *
 * Al arrancar la aplicacion se inicializan los semaforos leyendo de la base de
 * datos cuanto espacio esta ya ocupado, ya que los semaforos son en memoria y
 * no sobreviven a un reinicio del servidor.
 */
@Component
public class GestorEspacio {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    @Value("${drivepsp.storage.server-limit-mb:500}")
    private int limiteServidorMb;

    @Value("${drivepsp.storage.user-limit-mb:100}")
    private int limiteUsuarioMb;

    /** Semaforo global del servidor. Cada permiso equivale a 1 KB. */
    private Semaphore espacioServidor;

    /** Mapa de semaforos individuales, uno por cada usuario registrado. */
    private final Map<UUID, Semaphore> espacioUsuarios = new ConcurrentHashMap<>();

    public GestorEspacio(UserRepository userRepository, FileRepository fileRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    /**
     * Inicializa los semaforos al arrancar la aplicacion, restando el espacio
     * ya ocupado segun la base de datos.
     */
    @PostConstruct
    public void inicializar() {
        long bytesTotales = fileRepository.sumarBytesTotales();
        int kbOcupadosServidor = bytesAKb(bytesTotales);

        int permisosServidor = Math.max(0, limiteServidorMb * 1024 - kbOcupadosServidor);
        espacioServidor = new Semaphore(permisosServidor);

        List<User> usuarios = userRepository.findAll();
        for (User usuario : usuarios) {
            long bytesUsuario = fileRepository.sumarBytesPorUsuario(usuario.getId());
            int kbOcupados = bytesAKb(bytesUsuario);
            int permisosUsuario = Math.max(0, limiteUsuarioMb * 1024 - kbOcupados);
            espacioUsuarios.put(usuario.getId(), new Semaphore(permisosUsuario));
        }
    }

    /**
     * Registra un nuevo usuario creando su semaforo con el limite completo.
     */
    public void registrarUsuario(UUID userId, int limiteMb) {
        espacioUsuarios.put(userId, new Semaphore(limiteMb * 1024));
    }

    /**
     * Intenta reservar espacio para una subida. Primero comprueba el semaforo
     * del servidor y despues el del usuario. Si alguno falla, libera lo que
     * haya adquirido y devuelve false.
     *
     * @param userId id del usuario que sube el archivo
     * @param kb kilobytes necesarios, redondeados hacia arriba
     * @return true si se reservo el espacio, false si no hay sitio
     */
    public boolean reservar(UUID userId, int kb) {
        if (!espacioServidor.tryAcquire(kb)) {
            return false;
        }

        Semaphore espacioUsuario = espacioUsuarios.get(userId);
        if (espacioUsuario == null || !espacioUsuario.tryAcquire(kb)) {
            espacioServidor.release(kb);
            return false;
        }

        return true;
    }

    /**
     * Libera el espacio ocupado por un archivo devolviendo los permisos
     * a los dos semaforos.
     *
     * @param userId id del usuario que elimina el archivo
     * @param kb kilobytes que se liberan
     */
    public void liberar(UUID userId, int kb) {
        Semaphore espacioUsuario = espacioUsuarios.get(userId);
        if (espacioUsuario != null) {
            espacioUsuario.release(kb);
        }
        espacioServidor.release(kb);
    }

    /**
     * Convierte bytes a kilobytes redondeando hacia arriba. Usamos KB como
     * unidad del semaforo en lugar de MB para reducir el error de redondeo.
     */
    public static int bytesAKb(long bytes) {
        return (int) Math.ceil(bytes / 1024.0);
    }
}
