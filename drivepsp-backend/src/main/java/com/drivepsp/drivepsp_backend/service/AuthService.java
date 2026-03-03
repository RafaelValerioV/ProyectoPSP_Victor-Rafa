package com.drivepsp.drivepsp_backend.service;

import com.drivepsp.drivepsp_backend.concurrency.GestorEspacio;
import com.drivepsp.drivepsp_backend.dto.AuthResponse;
import com.drivepsp.drivepsp_backend.dto.LoginRequest;
import com.drivepsp.drivepsp_backend.dto.RegisterRequest;
import com.drivepsp.drivepsp_backend.dto.UserResponse;
import com.drivepsp.drivepsp_backend.entity.User;
import com.drivepsp.drivepsp_backend.repository.FileRepository;
import com.drivepsp.drivepsp_backend.repository.UserRepository;
import com.drivepsp.drivepsp_backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Gestiona el registro, el login y la consulta de datos del usuario autenticado.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GestorEspacio gestorEspacio;

    @Value("${drivepsp.storage.user-limit-mb:100}")
    private int limiteUsuarioMb;

    @Value("${drivepsp.storage.server-limit-mb:500}")
    private int limiteServidorMb;

    public AuthService(UserRepository userRepository,
                       FileRepository fileRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       GestorEspacio gestorEspacio) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.gestorEspacio = gestorEspacio;
    }

    /**
     * Registra un usuario nuevo comprobando que el email no exista ya,
     * guarda la contraseña hasheada con BCrypt y crea su semaforo de espacio
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user = userRepository.save(user);

        gestorEspacio.registrarUsuario(user.getId(), limiteUsuarioMb);

        String token = jwtTokenProvider.generarToken(user.getId());
        return new AuthResponse(token, user.getEmail(), user.getName());
    }

    /**
     * Verifica las credenciales del usuario y devuelve un token JWT si son correctas
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        String token = jwtTokenProvider.generarToken(user.getId());
        return new AuthResponse(token, user.getEmail(), user.getName());
    }

    /**
     * Devuelve los datos del usuario autenticado junto con su uso de espacio
     * y los totales del servidor
     */
    public UserResponse me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        long serverUsed = fileRepository.sumarBytesTotales();
        long serverLimit = (long) limiteServidorMb * 1024 * 1024;

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStorageUsed(),
                user.getStorageLimit(),
                serverUsed,
                serverLimit
        );
    }
}
