package com.githubzs.plataforma_reservas_medicas.Api.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.Api.dto.AppointmentDto.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.DoctorScheduleDto.DoctorScheduleSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.SpecialtyDto.SpecialtySummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DoctorDto {

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
    String fullName,        // null = no actualizar

    @Size(max = 320)
    String email,           // null = no actualizar

    @Size(max = 50)
    String licenseNumber,   // null = no actualizar

    @Size(max = 50)
    String documentNumber,  // null = no actualizar

    UUID specialtyId        // null = no actualizar
    ) implements Serializable {}

    public record DoctorResponse(
        UUID id, 
        String fullName, 
        String email,  
        SpecialtySummaryResponse specialty, 
        DoctorStatus status,  
        Instant createdAt,
        Instant updatedAt
    ) implements Serializable {}

    public record DoctorSummaryResponse(
        UUID id, 
        String fullName, 
        String email, 
        DoctorStatus status, 
        SpecialtySummaryResponse specialty
    ) implements Serializable {}

    // Por si se necesita una respuesta con más detalles, como las citas y horarios asociados al doctor
    public record DoctorDetailResponse(
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
}
