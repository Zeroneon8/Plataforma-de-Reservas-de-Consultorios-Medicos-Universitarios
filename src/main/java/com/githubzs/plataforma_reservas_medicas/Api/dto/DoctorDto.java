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
import jakarta.validation.constraints.Size;

public class DoctorDto {

    public record DoctorCreateRequest(
        @NotBlank
        @Size(max = 100)
        String fullname, 
        @Size(max = 320)
        String email,
        @NotBlank
        @Size(max = 50)  
        String licenceNumber, 
        @NotBlank
        @Size(max = 50)
        String documentNumber,
        @NotBlank  
        UUID specialtyId
    ) implements Serializable {}


    public record DoctorResponse(
        UUID id, 
        String fullname, 
        String email,  
        SpecialtySummaryResponse specialty, 
        DoctorStatus status,  
        Instant createdAt,
        Instant updatedAt
    ) implements Serializable {}

    public record DoctorSummaryResponse(
        UUID id, 
        String fullname, 
        String email, 
        DoctorStatus status, 
        SpecialtySummaryResponse specialty
    ) implements Serializable {}

    // Por si se necesita una respuesta con más detalles, como las citas y horarios asociados al doctor
    public record DoctorDetailResponse(
        UUID id, 
        String fullname, 
        String email,  
        SpecialtySummaryResponse specialty, 
        DoctorStatus status,  
        Instant createdAt,
        Instant updatedAt,
        Set<AppointmentSummaryResponse> appointments, 
        Set<DoctorScheduleSummaryResponse> schedules
    ) implements Serializable {}
}
