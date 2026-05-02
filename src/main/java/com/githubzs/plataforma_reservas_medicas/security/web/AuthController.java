package com.githubzs.plataforma_reservas_medicas.security.web;

import com.githubzs.plataforma_reservas_medicas.security.jwt.JwtService;
import com.githubzs.plataforma_reservas_medicas.security.domine.entities.AppUser;
import com.githubzs.plataforma_reservas_medicas.security.domine.entities.Role;
import com.githubzs.plataforma_reservas_medicas.security.domine.repository.AppUserRepository;
import com.githubzs.plataforma_reservas_medicas.security.dto.AuthDtos.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (appUserRepository.existsByDocumentNumberIgnoreCase(request.documentNumber())) {
            return ResponseEntity.badRequest().build();
        }

        var roles = Optional.ofNullable(request.roles()).filter(r -> !r.isEmpty())
            .orElseGet(() -> Set.of(Role.ROLE_STAFF));

        var user = AppUser.builder()
            .documentNumber(request.documentNumber())
            .password(passwordEncoder.encode(request.password()))
            .roles(roles)
            .build();

        appUserRepository.save(user);

        var principal = User.withUsername(user.getDocumentNumber())
            .password(user.getPassword())
            .authorities(roles.stream().map(Enum::name).toArray(String[]::new))
            .build();

        var token = jwtService.generateToken(principal, Map.of("roles", roles));
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.documentNumber(), request.password()));
        var user = appUserRepository.findByDocumentNumberIgnoreCase(request.documentNumber()).orElseThrow();
        var principal = User.withUsername(user.getDocumentNumber())
            .password(user.getPassword())
            .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
            .build();
        var token = jwtService.generateToken(principal, Map.of("roles", user.getRoles()));
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds()));    
    }

}
