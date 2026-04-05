package com.githubzs.plataforma_reservas_medicas.Api.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class AvailabilityDto {

    public record AvailabilitySlotResponse(
        UUID doctorId,
        String doctorFullName,
        UUID officeId,
        String officeName,
        LocalDate date,
        LocalTime slotStart,
        LocalTime slotEnd,
        int durationMinutes
    ) implements Serializable {}

    public record OfficeOccupancyResponse(
        UUID officeId,
        String officeName,
        String officeLocation,
        int roomNumber,
        LocalDate from,
        LocalDate to,
        int totalAppointments,
        int completedAppointments,
        int cancelledAppointments,
        int pendingAppointments,
        double occupancyPercentage
    ) implements Serializable {}

}