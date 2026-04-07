package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AppointmentDtos {
    
    public record AppointmentCreateRequest(
        @NotNull
        UUID doctorId, 
        @NotNull
        UUID patientId,
        @NotNull
        UUID officeId,
        @NotNull
        UUID appointmentTypeId,
        @NotNull
        LocalDateTime startAt     
    ) implements Serializable {}

    public record AppointmentCancelRequest(
        @NotBlank
        @Size(max = 1000)
        String cancelReason
    ) implements Serializable {}

    public record AppointmentCompleteRequest(
        @Size(max = 1000) 
        String observations
    ) implements Serializable {}

    public record AppointmentResponse(
        UUID id,
        PatientSummaryResponse patient,
        DoctorSummaryResponse doctor,
        OfficeSummaryResponse office,
        AppointmentTypeSummaryResponse appointmentType,
        LocalDateTime startAt,
        LocalDateTime endAt,
        AppointmentStatus status,
        String cancelReason,
        String observations,
        Instant createdAt,
        Instant updatedAt
    ) implements Serializable {}

    public record AppointmentSummaryResponse(
        UUID id,
        PatientSummaryResponse patient,
        DoctorSummaryResponse doctor,
        LocalDateTime startAt,
        LocalDateTime endAt,
        AppointmentStatus status,
        String cancelReason,
        String observations,
        Instant createdAt,
        Instant updatedAt
    ) implements Serializable {}

    public record AppointmentSearchRequest(
        UUID patientId,
        UUID doctorId,
        UUID officeId,
        UUID specialtyId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        AppointmentStatus status
    ) implements Serializable {}

}
