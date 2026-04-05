package com.githubzs.plataforma_reservas_medicas.Api.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.Api.dto.AppointmentDto.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class OfficeDto {

    public record OfficeCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotBlank
        @Size(max = 100)
        String location,
        @Size(max = 255)
        String description,
        @Positive
        int roomNumber
    ) implements Serializable {}

    public record OfficeUpdateRequest(
    @Size(max = 100)
    String name,            // null = no actualizar

    @Size(max = 100)
    String location,        // null = no actualizar

    @Size(max = 255)
    String description,     // null = no actualizar

    @Positive
    Integer roomNumber      // Integer (no int) para permitir null
    ) implements Serializable {}

    public record OfficeResponse(
        UUID id,
        String name,
        String location,
        String description,
        int roomNumber,
        OfficeStatus status,
        Instant createdAt,
        Instant updatedAt
    ) implements Serializable {}

    public record OfficeSummaryResponse(
        UUID id,
        String name,
        String location,
        int roomNumber,
        OfficeStatus status
    ) implements Serializable {}

    // Para GET /offices/{id} con sus citas asociadas
    public record OfficeDetailResponse(
        UUID id,
        String name,
        String location,
        String description,
        int roomNumber,
        OfficeStatus status,
        Instant createdAt,
        Instant updatedAt,
        Set<AppointmentSummaryResponse> appointments
    ) implements Serializable {}
}