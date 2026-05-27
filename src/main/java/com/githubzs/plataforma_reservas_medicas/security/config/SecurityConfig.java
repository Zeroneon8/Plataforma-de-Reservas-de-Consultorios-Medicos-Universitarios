package com.githubzs.plataforma_reservas_medicas.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.githubzs.plataforma_reservas_medicas.security.error.Http401EntryPoint;
import com.githubzs.plataforma_reservas_medicas.security.error.Http403AccessDenied;
import com.githubzs.plataforma_reservas_medicas.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Http401EntryPoint http401EntryPoint;
    private final Http403AccessDenied http403AccessDenied;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(http401EntryPoint)
                .accessDeniedHandler(http403AccessDenied))
            .authorizeHttpRequests(auth -> auth

                // ── Públicos ───────────────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/actuator/health").permitAll()

                // ── Pacientes: ADMIN y STAFF ───────────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/patients",
                    "/api/patients/{id}"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(HttpMethod.PATCH,
                    "/api/patients/{id}"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,
                    "/api/patients"
                ).hasRole("ADMIN")

                // ── Doctores: ADMIN y STAFF ────────────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/doctors",
                    "/api/doctors/{id}",
                    "/api/doctors/{doctorId}/schedules"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(HttpMethod.PATCH,
                    "/api/doctors/{id}"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,
                    "/api/doctors",
                    "/api/doctors/{doctorId}/schedules"
                ).hasRole("ADMIN")

                // ── Consultorios: ADMIN y STAFF ────────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/offices"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(HttpMethod.PATCH,
                    "/api/offices/{id}"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,
                    "/api/offices"
                ).hasRole("ADMIN")

                // ── Catálogos: ADMIN y STAFF ───────────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/specialties",
                    "/api/appointment-types"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(HttpMethod.POST,
                    "/api/specialties",
                    "/api/appointment-types"
                ).hasRole("ADMIN")

                // ── Disponibilidad: ADMIN y STAFF ──────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/availability/doctors/{doctorId}",
                    "/api/availability/doctors/{doctorId}/appointment-types/{appointmentTypeId}"
                ).hasAnyRole("ADMIN", "STAFF")

                // ── Citas: lectura — ADMIN, STAFF y DOCTOR ────────────────────────
                // DOCTOR solo necesita ver citas para poder completarlas.
                // El filtrado por doctor se maneja en el servicio con el JWT.
                .requestMatchers(HttpMethod.GET,
                    "/api/appointments",
                    "/api/appointments/{id}"
                ).hasAnyRole("ADMIN", "STAFF", "DOCTOR")

                // ── Citas: creación — ADMIN y STAFF ───────────────────────────────
                .requestMatchers(HttpMethod.POST,
                    "/api/appointments"
                ).hasAnyRole("ADMIN", "STAFF")

                // ── Citas: transiciones de estado ──────────────────────────────────
                .requestMatchers(HttpMethod.PATCH,
                    "/api/appointments/{id}/confirm",
                    "/api/appointments/{id}/cancel"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(HttpMethod.PATCH,
                    "/api/appointments/{id}/complete"
                ).hasRole("DOCTOR")

                .requestMatchers(HttpMethod.PATCH,
                    "/api/appointments/{id}/no-show"
                ).hasRole("ADMIN")

                // ── Reportes: ADMIN y COORDINATOR ─────────────────────────────────
                // COORDINATOR solo opera aquí. El dashboard lee este endpoint
                // para mostrarle el ranking de productividad.
                .requestMatchers(HttpMethod.GET,
                    "/api/reports/**"
                ).hasAnyRole("ADMIN", "COORDINATOR")

                // ── Cualquier otra ruta requiere autenticación ─────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}