package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ReportDtos {

    public record DoctorProductivityResponse(
        UUID doctorId,
        String doctorFullName,
        String specialtyName,
        LocalDate from,
        LocalDate to,
        int totalAppointments,
        int completedAppointments,
        int cancelledAppointments,
        int noShowAppointments,
        double completionRate,
        double cancellationRate,
        double averageDurationMinutes
    ) implements Serializable {}

    public record NoShowPatientResponse(
        UUID patientId,
        String patientFullName,
        String patientEmail,
        String patientPhoneNumber,
        String documentNumber,
        LocalDate from,
        LocalDate to,
        int totalNoShows,
        List<NoShowAppointmentDetail> appointments
    ) implements Serializable {

        public record NoShowAppointmentDetail(
            UUID appointmentId,
            String doctorFullName,
            String officeName,
            LocalDateTime scheduledAt
        ) implements Serializable {}
    }
}