package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.DashboardDtos.*;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;
import com.githubzs.plataforma_reservas_medicas.services.DashboardService;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.services.AppointmentService;
import com.githubzs.plataforma_reservas_medicas.services.DoctorService;
import com.githubzs.plataforma_reservas_medicas.services.PatientService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats(LocalDate date) {
        if (date == null) {
            throw new ValidationException("Date is required",
                List.of(new FieldViolation("date", "is required")));
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Date cannot be in the future",
                List.of(new FieldViolation("date", "cannot be in the future")));
        }

        long activeDoctors = doctorService.countByStatus(DoctorStatus.ACTIVE);
        long inactiveDoctors = doctorService.countByStatus(DoctorStatus.INACTIVE);
        long activePatients = patientService.countByStatus(PatientStatus.ACTIVE);
        long inactivePatients = patientService.countByStatus(PatientStatus.INACTIVE);
        long todayAppointments = appointmentService.countByDate(date);
        long todayCompletedAppointments = appointmentService.countByStatusAndDate(AppointmentStatus.COMPLETED, date);
        long todayScheduledAppointments = appointmentService.countByStatusAndDate(AppointmentStatus.SCHEDULED, date);

        return new DashboardResponse(
            activeDoctors,
            inactiveDoctors,
            activePatients,
            inactivePatients,
            todayAppointments,
            todayCompletedAppointments,
            todayScheduledAppointments
        );
    }

}
