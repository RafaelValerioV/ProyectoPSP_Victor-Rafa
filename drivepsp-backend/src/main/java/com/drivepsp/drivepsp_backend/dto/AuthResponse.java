package com.drivepsp.drivepsp_backend.dto;

/*Utilizamos "clases record" para simplificar, al final son DTOs para mapear*/
public record AuthResponse(
        String token,
        String email,
        String name
) {
}
