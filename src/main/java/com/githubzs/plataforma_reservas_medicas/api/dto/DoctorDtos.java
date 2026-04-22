package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtySummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DoctorDtos {

    public record DoctorCreateRequest(
        @NotBlank
        @Size(max = 100)
        String fullName, 
        @NotBlank
        @Size(max = 320)
        String email,
        @NotBlank
        @Size(max = 50)  
        String licenseNumber, 
        @NotBlank
        @Size(max = 50)
        String documentNumber,
        @NotNull  
        UUID specialtyId
    ) implements Serializable {}

    public record DoctorUpdateRequest(
        @Size(max = 100)
        String fullName,
        @Size(max = 320)
        String email,
        UUID specialtyId
    ) implements Serializable {}

    public record DoctorStatusUpdateRequest(
        @NotNull
        DoctorStatus status
    ) implements Serializable {}

    public record DoctorResponse(
        UUID id, 
        String fullName, 
        String email,  
        SpecialtySummaryResponse specialty, 
        DoctorStatus status,  
        Instant createdAt,
        Instant updatedAt,
        Set<AppointmentSummaryResponse> appointments, 
        Set<DoctorScheduleSummaryResponse> schedules
    ) implements Serializable {}

    public record DoctorSummaryResponse(
        UUID id, 
        String fullName, 
        String email,  
        SpecialtySummaryResponse specialty, 
        DoctorStatus status,  
        Instant createdAt,
        Instant updatedAt
    ) implements Serializable {}

}
