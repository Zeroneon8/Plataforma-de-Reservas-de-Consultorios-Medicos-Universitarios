package com.githubzs.plataforma_reservas_medicas.Repositories;

import java.time.Instant;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorScheduleRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;

class DoctorScheduleRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private SpecialtyRepository specialtyRepository;
    
    // Datos base reutilizados en todos los tests
    private Doctor doctor;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        specialty = specialtyRepository.save(
            Specialty.builder()
                .name("Medicina General")
                .build()
        );
        doctor = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. House")
                .documentNumber("123456")
                .licenseNumber("LIC-001")
                .email("drhouse@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );
    }


    @Test
    @DisplayName("Doctor Schedule: Encuentra los horarios de un doctor por el día de la semana")
    void shouldFindByDoctorIdAndDayOfWeek() {
        // Given
        var doctorSchedule = doctorScheduleRepository.save(
            DoctorSchedule.builder()
             .doctor(doctor)
             .dayOfWeek(DayOfWeek.MONDAY)
             .startTime(LocalTime.of(9, 0))
             .endTime(LocalTime.of(11, 0))
             .build()
            );

        var doctorSchedule2 = doctorScheduleRepository.save(
            DoctorSchedule.builder()
             .doctor(doctor)
             .dayOfWeek(DayOfWeek.MONDAY)
             .startTime(LocalTime.of(13, 0))
             .endTime(LocalTime.of(15, 0))
             .build()
            );
        
        // When
        var schedules = doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctor.getId(), doctorSchedule.getDayOfWeek());
        
        // Then
        assertThat(schedules).hasSize(2);
        assertThat(schedules).extracting(DoctorSchedule::getId)
            .containsExactlyInAnyOrder(doctorSchedule.getId(), doctorSchedule2.getId());
        assertThat(schedules).extracting(DoctorSchedule::getStartTime)
            .containsExactlyInAnyOrder(doctorSchedule.getStartTime(), doctorSchedule2.getStartTime());
        assertThat(schedules).extracting(DoctorSchedule::getEndTime)
            .containsExactlyInAnyOrder(doctorSchedule.getEndTime(), doctorSchedule2.getEndTime());
        assertThat(schedules).extracting(ds -> ds.getDoctor().getId())
            .containsOnly(doctor.getId());
        assertThat(schedules).extracting(DoctorSchedule::getDayOfWeek)
            .containsOnly(doctorSchedule.getDayOfWeek());  
    }

    @Test
    @DisplayName("Doctor Schedule: No encuentra horarios para un doctor en un día sin horarios")
    void shouldReturnEmptyWhenNoSchedulesFoundForDoctorOnDay() {
        // Given
        doctorScheduleRepository.save(
            DoctorSchedule.builder()
             .doctor(doctor)
             .dayOfWeek(DayOfWeek.MONDAY)
             .startTime(LocalTime.of(9, 0))
             .endTime(LocalTime.of(11, 0))
             .build()
            );
        
        // When
        var schedules = doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctor.getId(), DayOfWeek.TUESDAY);
        
        // Then
        assertThat(schedules).isEmpty();
    }

    @Test
    @DisplayName("Doctor Schedule: No encuentra horarios cuando el día de la semana coincide pero el doctor es diferente")
    void shouldReturnEmptyWhenNoSchedulesFoundForDoctorOnDayWithDifferentDoctor() {
        // Given
        doctorScheduleRepository.save(
            DoctorSchedule.builder()
             .doctor(doctor)
             .dayOfWeek(DayOfWeek.MONDAY)
             .startTime(LocalTime.of(9, 0))
             .endTime(LocalTime.of(11, 0))
             .build()
            );

        var altId = new UUID(doctor.getId().getMostSignificantBits(), doctor.getId().getLeastSignificantBits() + 1);
        
        // When
        var schedules = doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(altId, DayOfWeek.MONDAY);
        
        // Then
        assertThat(schedules).isEmpty();
    }

}
