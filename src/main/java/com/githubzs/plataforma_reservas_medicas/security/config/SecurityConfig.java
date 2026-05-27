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

                // ── Solo lectura: ADMIN y STAFF ────────────────────────────────────
                // Recursos que el doctor no necesita ver
                .requestMatchers(HttpMethod.GET,
                    "/api/patients", "/api/patients/{id}",
                    "/api/offices",
                    "/api/doctors/{doctorId}/schedules",
                    "/api/availability/doctors/{doctorId}",
                    "/api/availability/doctors/{doctorId}/appointment-types/{appointmentTypeId}"
                ).hasAnyRole("ADMIN", "STAFF")

                // ── Solo lectura: ADMIN, STAFF y DOCTOR ───────────────────────────
                // El doctor necesita ver citas para completarlas,
                // y los catálogos para entender el contexto de cada cita.
                .requestMatchers(HttpMethod.GET,
                    "/api/appointments", "/api/appointments/{id}",
                    "/api/doctors", "/api/doctors/{id}",
                    "/api/specialties",
                    "/api/appointment-types"
                ).hasAnyRole("ADMIN", "STAFF", "DOCTOR")

                // ── Acciones sobre citas: ADMIN y STAFF ───────────────────────────
                .requestMatchers(HttpMethod.PATCH,
                    "/api/appointments/{id}/confirm",
                    "/api/appointments/{id}/cancel"
                ).hasAnyRole("ADMIN", "STAFF")

                // ── Completar cita: solo DOCTOR ────────────────────────────────────
                .requestMatchers(HttpMethod.PATCH,
                    "/api/appointments/{id}/complete"
                ).hasRole("DOCTOR")

                // ── No-show y edición de entidades: solo ADMIN ────────────────────
                .requestMatchers(HttpMethod.PATCH,
                    "/api/patients/{id}",
                    "/api/doctors/{id}",
                    "/api/offices/{id}",
                    "/api/appointments/{id}/no-show"
                ).hasRole("ADMIN")

                // ── Creación: ADMIN y STAFF ────────────────────────────────────────
                .requestMatchers(HttpMethod.POST,
                    "/api/appointments"
                ).hasAnyRole("ADMIN", "STAFF")

                // ── Creación de entidades maestras: solo ADMIN ────────────────────
                .requestMatchers(HttpMethod.POST,
                    "/api/patients", "/api/doctors", "/api/offices",
                    "/api/specialties", "/api/appointment-types",
                    "/api/doctors/{doctorId}/schedules"
                ).hasRole("ADMIN")

                // ── Reportes: ADMIN y COORDINATOR ─────────────────────────────────
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