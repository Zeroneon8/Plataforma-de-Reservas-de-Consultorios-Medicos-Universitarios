package com.githubzs.plataforma_reservas_medicas.api.dto;


import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDto.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PatientDto {
    
    public record PatientCreateRequest(
        @NotBlank
        @Size(max = 100)
        String fullName, 
        @NotBlank
        @Size(max = 320)
        String email,
        @NotBlank
        @Size(max = 20) 
        String phoneNumber, 
        @NotBlank
        @Size(max = 50)
        String documentNumber, 
        @NotBlank
        @Size(max = 50)
        String studentCode
    ) implements Serializable {}

    public record PatientUpdateRequest(
    @Size(max = 100)
    String fullName,        // null = no actualizar

    @Size(max = 320)
    String email,           // null = no actualizar

    @Size(max = 20)
    String phoneNumber,     // null = no actualizar

    @Size(max = 50)
    String documentNumber,  // null = no actualizar

    @Size(max = 50)
    String studentCode      // null = no actualizar
    ) implements Serializable {}

       
    public record PatientResponse(
        UUID id, 
        String fullName, 
        String email, 
        String phoneNumber, 
        String documentNumber, 
        String studentCode, 
        PatientStatus status, 
        Instant createdAt, 
        Instant updatedAt
    ) implements Serializable {}

    public record PatientSummaryResponse(
        UUID id, 
        String fullName, 
        String email, 
        String phoneNumber, 
        String documentNumber, 
        String studentCode, 
        PatientStatus status
    ) implements Serializable {}
    
    // Por si se necesita una respuesta con más detalles, como las citas asociadas al paciente
    public record PatientDetailResponse(
        UUID id, 
        String fullName,
        String email, 
        String phoneNumber, 
        String documentNumber, 
        String studentCode, 
        PatientStatus status, 
        Instant createdAt, 
        Instant updatedAt, 
        Set<AppointmentSummaryResponse> appointments
    ) implements Serializable {}
}
