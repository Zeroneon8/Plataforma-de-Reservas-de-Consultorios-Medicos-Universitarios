package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;

@SpringBootTest
class DoctorScheduleSummaryMapperTest {

    @Autowired
    private DoctorScheduleSummaryMapper mapper;

    @Test
    void toSummaryResponseShouldMapScheduleToSummary() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        Instant now = Instant.now();

        Specialty specialty = Specialty.builder()
            .id(specialtyId)
            .name("Medicina General")
            .build();

        Doctor doctor = Doctor.builder()
            .id(doctorId)
            .fullName("Dr. House")
            .email("house@example.com")
            .specialty(specialty)
            .status(DoctorStatus.ACTIVE)
            .createdAt(now)
            .build();

        DoctorSchedule schedule = DoctorSchedule.builder()
            .id(scheduleId)
            .doctor(doctor)
            .dayOfWeek(DayOfWeek.MONDAY)
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(17, 0))
            .build();

        // When
        DoctorScheduleSummaryResponse response = mapper.toSummaryResponse(schedule);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(scheduleId);
        assertThat(response.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(response.startTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(17, 0));
    }

}
