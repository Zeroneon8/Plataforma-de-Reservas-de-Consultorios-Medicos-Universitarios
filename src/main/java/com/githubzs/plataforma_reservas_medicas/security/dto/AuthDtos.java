package com.githubzs.plataforma_reservas_medicas.security.dto;

import java.util.Set;

import com.githubzs.plataforma_reservas_medicas.security.domine.entities.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
        @NotBlank
        @Size(max = 50)
        String documentNumber,
        @NotBlank
        @Size(max = 64)
        String password,
        Set<Role> roles
    ) {}
    
    public record LoginRequest(
        @NotBlank
        @Size(max = 50)
        String documentNumber,
        @NotBlank
        @Size(max = 64)
        String password
    ) {}

    public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
    ) {}

}
