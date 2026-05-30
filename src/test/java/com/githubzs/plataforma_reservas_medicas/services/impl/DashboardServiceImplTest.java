package com.githubzs.plataforma_reservas_medicas.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.DashboardDtos.DashboardResponse;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.services.AppointmentService;
import com.githubzs.plataforma_reservas_medicas.services.DoctorService;
import com.githubzs.plataforma_reservas_medicas.services.PatientService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private PatientService patientService;
    @Mock
    private DoctorService doctorService;
    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private DashboardServiceImpl service;

    @Test
    void shouldReturnDashboardStatsWhenDateIsValid() {
        LocalDate date = LocalDate.now().minusDays(1);

        when(doctorService.countByStatus(DoctorStatus.ACTIVE)).thenReturn(3L);
        when(doctorService.countByStatus(DoctorStatus.INACTIVE)).thenReturn(1L);
        when(patientService.countByStatus(PatientStatus.ACTIVE)).thenReturn(10L);
        when(patientService.countByStatus(PatientStatus.INACTIVE)).thenReturn(2L);
        when(appointmentService.countByDate(date)).thenReturn(12L);
        when(appointmentService.countByStatusAndDate(AppointmentStatus.COMPLETED, date)).thenReturn(4L);
        when(appointmentService.countByStatusAndDate(AppointmentStatus.SCHEDULED, date)).thenReturn(8L);

        DashboardResponse result = service.getDashboardStats(date);

        assertEquals(3L, result.activeDoctors());
        assertEquals(1L, result.inactiveDoctors());
        assertEquals(10L, result.activePatients());
        assertEquals(2L, result.inactivePatients());
        assertEquals(12L, result.todayAppointments());
        assertEquals(4L, result.todayCompletedAppointments());
        assertEquals(8L, result.todayScheduledAppointments());

        verify(doctorService).countByStatus(DoctorStatus.ACTIVE);
        verify(doctorService).countByStatus(DoctorStatus.INACTIVE);
        verify(patientService).countByStatus(PatientStatus.ACTIVE);
        verify(patientService).countByStatus(PatientStatus.INACTIVE);
        verify(appointmentService).countByDate(date);
        verify(appointmentService).countByStatusAndDate(AppointmentStatus.COMPLETED, date);
        verify(appointmentService).countByStatusAndDate(AppointmentStatus.SCHEDULED, date);
    }

    @Test
    void shouldThrowValidationExceptionWhenDateIsNull() {
        assertThrows(ValidationException.class, () -> service.getDashboardStats(null));
    }

    @Test
    void shouldThrowValidationExceptionWhenDateIsInFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThrows(ValidationException.class, () -> service.getDashboardStats(futureDate));
    }
}
