package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;

public class DoctorScheduleDtos {

    public record DoctorScheduleCreateRequest(
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