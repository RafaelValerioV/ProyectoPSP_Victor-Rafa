package com.drivepsp.drivepsp_backend.controller;

import com.drivepsp.drivepsp_backend.dto.AuthResponse;
import com.drivepsp.drivepsp_backend.dto.LoginRequest;
import com.drivepsp.drivepsp_backend.dto.RegisterRequest;
import com.drivepsp.drivepsp_backend.dto.UserResponse;
import com.drivepsp.drivepsp_backend.service.AuthService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticacion con tres endpoints: registro, login y
 * consulta del usuario autenticado.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserResponse response = authService.me(userId);
        return ResponseEntity.ok(response);
    }
}
