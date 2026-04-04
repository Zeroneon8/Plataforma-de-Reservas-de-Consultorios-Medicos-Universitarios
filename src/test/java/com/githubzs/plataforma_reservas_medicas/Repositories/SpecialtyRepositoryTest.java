package com.githubzs.plataforma_reservas_medicas.Repositories;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;

public class SpecialtyRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Debe retornar si existe una especialidad con el nombre dado")
    public void shouldExistsByName() {
       
        var specialty= specialtyRepository.save(Specialty.builder().name("Cardiología").build());
        var specialty2= Specialty.builder().name("Dermatología").build();
        
        boolean exists = specialtyRepository.existsByName(specialty.getName());
        boolean exists2 = specialtyRepository.existsByName(specialty2.getName());

        assertThat(exists).isTrue();
        assertThat(exists2).isFalse();
    }

        @Test
        @DisplayName("Debe retornar las estadísticas de asistencia por especialidad con citas canceladas y no-shows")
        public void shouldFoundSpecialtyAttendanceStatsByCancelledAndNoShow() {

            var now = LocalDateTime.now();
            var office1 = officeRepository.save(
                Office.builder()
                .name("Consultorio 1")
                .location("Edificio A")
                .roomNumber(101)
                .createdAt(Instant.now())
                .status(OfficeStatus.AVAILABLE)
                .build()
            );

            var appointmentType = appointmentTypeRepository.save(
                AppointmentType.builder()
                .name("Consulta general")
                .durationMinutes(30)
                .build()
            );

            var patient = patientRepository.save(
                Patient.builder()
                .fullName("John Doe")
                .documentNumber("123456")
                .phoneNumber("324-123-4567")
                .email("JohnDoe123@gmail.com")
                .createdAt(Instant.now())
                .status(com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus.ACTIVE)
                .build()
            );

            var specialty = specialtyRepository.save(
                Specialty.builder()
                .name("Medicina General")
                 .build()
            );

            var specialty2 = specialtyRepository.save(
                Specialty.builder()
                .name("psicologia")
                 .build()
            );

            var doctor = doctorRepository.save(
                Doctor.builder()
                .fullName("Dr. House")
                .documentNumber("22222")
                .email("drHouse123@doctor.com")
                .licenseNumber("11")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
            );

             var doctor2 = doctorRepository.save(
                Doctor.builder()
                .fullName("Meredith Grey")
                .documentNumber("111111")
                .email("meredith.grey@doctor.com")
                .licenseNumber("12")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty2)
                .createdAt(Instant.now())
                .build()
            );

        
            // CITA CONFIRMED
            appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor2)
            .office(office1)
            .appointmentType(appointmentType)
            .startAt(now)
            .endAt(now.plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.CONFIRMED)
            .createdAt(Instant.now())
            .build()
        );

        // Cita NO_SHOW 
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office1)
            .appointmentType(appointmentType)
            .startAt(now)
            .endAt(now.plusMinutes(30))
            .status(AppointmentStatus.NO_SHOW)
            .createdAt(Instant.now())
            .build()
        );

        // Cita CANCELLED 
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office1)
            .appointmentType(appointmentType)
            .startAt(now)
            .endAt(now.plusMinutes(30))
            .status(AppointmentStatus.CANCELLED)
            .createdAt(Instant.now())
            .build()
        );

        // Cita CONFIRMED
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office1)
            .appointmentType(appointmentType)
            .startAt(now.plusDays(5))
            .endAt(now.plusDays(5).plusMinutes(30))
            .status(AppointmentStatus.CONFIRMED)
            .createdAt(Instant.now())
            .build()
        );

        // Cita NO SHOW
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor2)
            .office(office1)
            .appointmentType(appointmentType)
            .startAt(now)
            .endAt(now.plusMinutes(30))
            .status(AppointmentStatus.NO_SHOW)
            .createdAt(Instant.now())
            .build()
        );

            var stats = specialtyRepository.countCancelledAndNoShowBySpecialty();
    
            assertThat(stats).hasSize(2);
            var statPsi = stats.get(1);
            assertThat(statPsi.getSpecialtyName()).isEqualTo("psicologia");
            assertThat(statPsi.getCancelledCount()).isEqualTo(0);
            assertThat(statPsi.getNoShowCount()).isEqualTo(1);

            var statMed = stats.get(0);
            assertThat(statMed.getSpecialtyName()).isEqualTo("Medicina General");
            assertThat(statMed.getCancelledCount()).isEqualTo(1);
            assertThat(statMed.getNoShowCount()).isEqualTo(1);

            // Verificar que las especialidades correctas están en el resultado
            assertThat(stats).extracting(s -> s.getSpecialtyName())
                .containsExactlyInAnyOrder("psicologia", "Medicina General");

            // Verificar que ninguna especialidad tiene conteos negativos
            assertThat(stats).extracting(s -> s.getCancelledCount())
                .allMatch(count -> count >= 0);

            assertThat(stats).extracting(s -> s.getNoShowCount())
                .allMatch(count -> count >= 0);

            // Verificar que las citas CONFIRMED no se cuentan en ninguno de los dos conteos
            assertThat(statPsi.getCancelledCount() + statPsi.getNoShowCount()).isEqualTo(1); // solo 1 NO_SHOW, la CONFIRMED no cuenta
            assertThat(statMed.getCancelledCount() + statMed.getNoShowCount()).isEqualTo(2); // 1 CANCELLED + 1 NO_SHOW, las CONFIRMED no cuentan

            // Verificar que no hay una tercera especialidad fantasma en los resultados
            assertThat(stats).extracting(s -> s.getSpecialtyName())
                .doesNotContain("Cardiología", "Pediatría"); 

            // Verificar que psicologia no tiene cancelled
            assertThat(statPsi.getCancelledCount()).isZero();            
        
        }
    
}
