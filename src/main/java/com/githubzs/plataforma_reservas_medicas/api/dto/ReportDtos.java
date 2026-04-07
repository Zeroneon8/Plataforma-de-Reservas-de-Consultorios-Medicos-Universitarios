package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;
import java.util.UUID;

public class ReportDtos {

    public record DoctorProductivityResponse(
        long rankingPosition,
        UUID doctorId,
        String doctorFullName,
        long completedAppointments
    ) implements Serializable {}

    public record NoShowPatientResponse(
        UUID patientId,
        String patientFullName,
        long totalNoShows
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