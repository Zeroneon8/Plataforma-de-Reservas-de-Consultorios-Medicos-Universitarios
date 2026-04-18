package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.DoctorProductivityResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.NoShowPatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.OfficeOccupancyResponse;
import com.githubzs.plataforma_reservas_medicas.domine.dto.DoctorRankingStatsDto;
import com.githubzs.plataforma_reservas_medicas.domine.dto.OfficeOccupancyDto;
import com.githubzs.plataforma_reservas_medicas.domine.dto.PatientNoShowStatsDto;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ReportServiceImpl service;

    private LocalDate baseDate;

    @BeforeEach
    void setUp() {
        // Use fixed base date to avoid flakiness
        baseDate = LocalDate.of(2026, 4, 1);
    }

    @Test
    void getOfficeOccupancyShouldReturnOccupancyData() {
        LocalDate from = baseDate;
        LocalDate to = baseDate.plusDays(2);

        UUID officeId = UUID.randomUUID();
        OfficeOccupancyDto dto = new OfficeOccupancyDto(officeId, "Office 1", "Building A", 101, 5L, 150L, 1L);

        LocalDateTime expectedFrom = from.atStartOfDay();
        LocalDateTime expectedTo = to.plusDays(1).atStartOfDay().minusSeconds(1);

        when(officeRepository.calculateOfficeOccupancyBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    LocalDateTime f = invocation.getArgument(0);
                    LocalDateTime t = invocation.getArgument(1);
                    if (expectedFrom.equals(f) && expectedTo.equals(t)) {
                        return List.of(dto);
                    }
                    return List.of();
                });

        List<OfficeOccupancyResponse> results = service.getOfficeOccupancy(from, to);

        assertEquals(1, results.size());
        OfficeOccupancyResponse r = results.get(0);
        assertEquals(officeId, r.officeId());
        assertEquals("Office 1", r.officeName());
        assertEquals("Building A", r.officeLocation());
        assertEquals(101, r.roomNumber());
        assertEquals(5L, r.appointmentCount());
        assertEquals(150L, r.minutesOccupied());
        assertEquals(1L, r.noShowCount());

        verify(officeRepository).calculateOfficeOccupancyBetween(expectedFrom, expectedTo);
    }

    @Test
    void getDoctorProductivityShouldReturnProductivityData() {
        DoctorRankingStatsDto d1 = new DoctorRankingStatsDto(UUID.randomUUID(), "Dr. Alpha", 10L);
        DoctorRankingStatsDto d2 = new DoctorRankingStatsDto(UUID.randomUUID(), "Dr. Beta", 7L);

        when(doctorRepository.rankDoctorsByCompletedAppointments())
                .thenReturn(List.of(d1, d2));

        List<DoctorProductivityResponse> results = service.getDoctorProductivity();

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).rankingPosition());
        assertEquals(d1.getDoctorId(), results.get(0).doctorId());
        assertEquals(d1.getDoctorName(), results.get(0).doctorFullName());
        assertEquals(10L, results.get(0).completedAppointments());

        assertEquals(2L, results.get(1).rankingPosition());
        assertEquals(d2.getDoctorId(), results.get(1).doctorId());
        assertEquals(d2.getDoctorName(), results.get(1).doctorFullName());
        assertEquals(7L, results.get(1).completedAppointments());

        verify(doctorRepository).rankDoctorsByCompletedAppointments();
    }

    @Test
    void getNoShowPatientsShouldReturnNoShowData() {
        LocalDate from = baseDate.minusDays(3);
        LocalDate to = baseDate;

        UUID patientId = UUID.randomUUID();
        PatientNoShowStatsDto dto = new PatientNoShowStatsDto(patientId, "Jane Doe", 2L);

        LocalDateTime expectedFrom = from.atStartOfDay();
        LocalDateTime expectedTo = to.plusDays(1).atStartOfDay().minusSeconds(1);

        when(patientRepository.countPatientsNoShow(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    LocalDateTime f = invocation.getArgument(0);
                    LocalDateTime t = invocation.getArgument(1);
                    if (expectedFrom.equals(f) && expectedTo.equals(t)) {
                        return List.of(dto);
                    }
                    return List.of();
                });

        List<NoShowPatientResponse> results = service.getNoShowPatients(from, to);

        assertEquals(1, results.size());
        NoShowPatientResponse r = results.get(0);
        assertEquals(patientId, r.patientId());
        assertEquals("Jane Doe", r.patientFullName());
        assertEquals(2L, r.totalNoShows());

        verify(patientRepository).countPatientsNoShow(expectedFrom, expectedTo);
    }

    @Test
    void getOfficeOccupancyShouldThrowWhenFromIsNull() {
        LocalDate to = baseDate;
        assertThrows(NullPointerException.class, () -> service.getOfficeOccupancy(null, to));
        verify(officeRepository, never()).calculateOfficeOccupancyBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getOfficeOccupancyShouldThrowWhenFromAfterTo() {
        LocalDate from = baseDate.plusDays(5);
        LocalDate to = baseDate;
        assertThrows(IllegalArgumentException.class, () -> service.getOfficeOccupancy(from, to));
        verify(officeRepository, never()).calculateOfficeOccupancyBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

}
