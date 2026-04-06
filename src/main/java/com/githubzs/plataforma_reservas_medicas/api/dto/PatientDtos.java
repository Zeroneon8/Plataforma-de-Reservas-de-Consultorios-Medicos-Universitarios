package com.githubzs.plataforma_reservas_medicas.api.dto;


import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PatientDtos {
    
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
        String studentCode
    ) implements Serializable {}

    public record PatientUpdateRequest(
        @Size(max = 100)
        String fullName,
        @Size(max = 320)
        String email,
        @Size(max = 20)
        String phoneNumber
    ) implements Serializable {}

       
    public record PatientResponse(
        UUID id, 
        String fullName, 
        String email, 
        String phoneNumber,
        PatientStatus status, 
        Instant createdAt, 
        Instant updatedAt,
        Set<AppointmentSummaryResponse> appointments
    ) implements Serializable {}

    public record PatientSummaryResponse(
        UUID id, 
        String fullName, 
        String email, 
        String phoneNumber,
        PatientStatus status, 
        Instant createdAt, 
        Instant updatedAt
    ) implements Serializable {}

}
