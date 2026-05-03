package com.githubzs.plataforma_reservas_medicas.security.config;

import com.githubzs.plataforma_reservas_medicas.security.error.Http401EntryPoint;
import com.githubzs.plataforma_reservas_medicas.security.error.Http403AccessDenied;
import com.githubzs.plataforma_reservas_medicas.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
            .exceptionHandling(ex -> ex.authenticationEntryPoint(http401EntryPoint)
                .accessDeniedHandler(http403AccessDenied))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                // Endpoints protegidos por rol
                .requestMatchers(HttpMethod.GET, "/api/patients", "/api/patients/{id}", "/api/doctors", "/api/doctors/{id}",
                                "/api/specialties", "/api/offices", "/api/appointment-types", "/api/doctors/{doctorId}/schedules",
                                "/api/appointments", "/api/appointments/{id}", "/api/availability/doctors/{doctorId}",
                                "/api/availability/doctors/{doctorId}/appointment-types/{appointmentTypeId}").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PATCH, "/api/appointments/{id}/confirm", "/api/appointments/{id}/cancel").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PATCH, "/api/appointments/{id}/complete").hasRole("DOCTOR")
                .requestMatchers(HttpMethod.PATCH, "/api/patients/{id}", "/api/doctors/{id}", "/api/offices/{id}",
                                "/api/appointments/{id}/no-show").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/appointments").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/patients", "/api/doctors", "/api/offices", "/api/specialties",
                                "/api/appointment-types", "/api/doctors/{doctorId}/schedules").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/reports/**").hasAnyRole("ADMIN", "COORDINATOR")
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
