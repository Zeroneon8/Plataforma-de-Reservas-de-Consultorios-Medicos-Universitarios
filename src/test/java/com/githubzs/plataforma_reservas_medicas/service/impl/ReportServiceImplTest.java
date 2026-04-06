package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.DoctorProductivityResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.NoShowPatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.OfficeOccupancyResponse;
import com.githubzs.plataforma_reservas_medicas.domine.dto.DoctorRankingStatsDto;
import com.githubzs.plataforma_reservas_medicas.domine.dto.OfficeOccupancyDto;
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

    @BeforeEach
    void setUp() {
    }

    @Test
    void getOfficeOccupancyShouldReturnOccupancyData() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        UUID officeId = UUID.randomUUID();
        OfficeOccupancyDto dto = new OfficeOccupancyDto(officeId, "Office 1", "Building A", 101, 5L, 150L, 1L);

        when(officeRepository.calculateOfficeOccupancyBetween(
                from.atStartOfDay(),
                to.atTime(java.time.LocalTime.MAX)))
                .thenReturn(List.of(dto));

        List<OfficeOccupancyResponse> results = service.getOfficeOccupancy(from, to);

        assertEquals(1, results.size());
        assertEquals(officeId, results.get(0).officeId());
        assertEquals("Office 1", results.get(0).officeName());
        assertEquals(5, results.get(0).appointmentCount());
    }

    @Test
    void getDoctorProductivityShouldReturnProductivityData() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        UUID doctorId = UUID.randomUUID();
        DoctorRankingStatsDto dto = new DoctorRankingStatsDto(doctorId, "Dr. Smith", 15L);

        when(doctorRepository.rankDoctorsByCompletedAppointments())
                .thenReturn(List.of(dto));

        List<DoctorProductivityResponse> results = service.getDoctorProductivity();

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).rankingPosition());
        assertEquals(doctorId, results.get(0).doctorId());
        assertEquals("Dr. Smith", results.get(0).doctorFullName());
        assertEquals(15, results.get(0).completedAppointments());
    }

    @Test
    void getNoShowPatientsShouldReturnNoShowData() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        UUID patientId = UUID.randomUUID();

        when(patientRepository.countPatientsNoShow(
                from.atStartOfDay(),
                to.atTime(java.time.LocalTime.MAX)))
                .thenReturn(new ArrayList<>());

        List<NoShowPatientResponse> results = service.getNoShowPatients(from, to);

        assertEquals(0, results.size());
    }

}
