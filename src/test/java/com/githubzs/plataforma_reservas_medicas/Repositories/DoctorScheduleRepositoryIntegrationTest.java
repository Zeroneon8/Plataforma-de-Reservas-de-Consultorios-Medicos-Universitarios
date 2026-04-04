package com.githubzs.plataforma_reservas_medicas.Repositories;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
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

public class DoctorScheduleRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;
    
    @Test
    @DisplayName("Deve retornar los horários de um médico para um dia específico")
    public void shoulFindByDoctorIdAndDayOfWeek() {
        
        var specialty = specialtyRepository.save(
            Specialty.builder()
                .name("Medicina General")
                .build()
        );

        var doctor = doctorRepository.save(
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

        var doctorSchedule = doctorScheduleRepository.save(
            DoctorSchedule.builder()
                 .doctor(doctor)
                 .dayOfWeek(java.time.DayOfWeek.MONDAY)
                 .startTime(java.time.LocalTime.of(9, 0))
                 .endTime(java.time.LocalTime.of(11, 0))
                .build()
            );

        var doctorSchedule2 = doctorScheduleRepository.save(
            DoctorSchedule.builder()
             .doctor(doctor)
             .dayOfWeek(java.time.DayOfWeek.MONDAY)
             .startTime(java.time.LocalTime.of(13, 0))
             .endTime(java.time.LocalTime.of(15, 0))
             .build()
            );
        
        var schedules = doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctor.getId(), doctorSchedule.getDayOfWeek());

        assertThat(schedules).isNotEmpty();
        assertThat(schedules).hasSize(2);
        assertThat(schedules.get(0).getDoctor().getId()).isEqualTo(doctor.getId());
        assertThat(schedules.get(0).getDayOfWeek()).isEqualTo(doctorSchedule.getDayOfWeek());

        assertThat(schedules.get(1).getDoctor().getId()).isEqualTo(doctor.getId());
        assertThat(schedules.get(1).getDayOfWeek()).isEqualTo(doctorSchedule2.getDayOfWeek());

        assertThat(schedules.get(0).getStartTime()).isEqualTo(doctorSchedule.getStartTime());
        assertThat(schedules.get(0).getEndTime()).isEqualTo(doctorSchedule.getEndTime());    
    }
    
}
