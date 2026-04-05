package com.githubzs.plataforma_reservas_medicas.Api.dto;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.Api.dto.AppointmentDto.AppointmentSummaryResponse;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AppointmentTypeDto {

    public record AppointmentTypeCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @Size(max = 255)
        String description,
        @Positive
        int durationMinutes
    ) implements Serializable {}

    public record AppointmentTypeResponse(
        UUID id,
        String name,
        String description,
        int durationMinutes
    ) implements Serializable {}

    public record AppointmentTypeDetailResponse(
        UUID id,
        String name,
        String description,
        int durationMinutes,
        Set<AppointmentSummaryResponse> appointments
    ) implements Serializable {}
    
}