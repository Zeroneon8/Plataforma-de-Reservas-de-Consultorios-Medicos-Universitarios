package com.githubzs.plataforma_reservas_medicas.Api.dto;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.Api.dto.DoctorDto.DoctorSummaryResponse;

public class DoctorScheduleDto {

    public record DoctorScheduleCreateRequest(
        UUID doctorId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
    ) implements Serializable {}

    public record DoctorScheduleResponse(
        UUID id,
        DoctorSummaryResponse doctor,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
    ) implements Serializable {}

    public record DoctorScheduleSummaryResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
    ) implements Serializable {}
}