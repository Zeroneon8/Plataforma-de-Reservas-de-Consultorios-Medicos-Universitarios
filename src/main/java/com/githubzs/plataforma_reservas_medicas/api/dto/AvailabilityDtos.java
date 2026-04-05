package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class AvailabilityDtos {

    public record AvailabilitySlotResponse(
        LocalDate date,
        LocalTime slotStart,
        LocalTime slotEnd
    ) implements Serializable {}

    public record OfficeOccupancyResponse(
        UUID officeId,
        String officeName,
        String officeLocation,
        int roomNumber,
        long appointmentCount,
        long minutesOccupied,
        long noShowCount
    ) implements Serializable {}

}