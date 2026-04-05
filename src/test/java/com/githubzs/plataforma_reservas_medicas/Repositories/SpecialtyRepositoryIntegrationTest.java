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
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;

class SpecialtyRepositoryIntegrationTest extends AbstractRepositoryIT {

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

    // Datos base reutilizados en test de conteo
    private Office office;
    private AppointmentType appointmentType;
    private Patient patient;
    private Specialty specialty;
    private Doctor doctor;

    // Helper para crear datos base para los test de conteo
    private void setUp() {
        office = officeRepository.save(
            Office.builder()
                .name("Consultorio 1")
                .location("Edificio A")
                .roomNumber(101)
                .createdAt(Instant.now())
                .status(OfficeStatus.AVAILABLE)
                .build()
        );

        appointmentType = appointmentTypeRepository.save(
            AppointmentType.builder()
                .name("Consulta general")
                .durationMinutes(30)
                .build()
        );

        patient = patientRepository.save(
            Patient.builder()
                .fullName("John Doe")
                .documentNumber("123456")
                .phoneNumber("3241234567")
                .email("JohnDoe123@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );

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
    @DisplayName("Specialty: Detecta si existe una especialidad por su nombre")
    void shouldExistsByName() {
        // Given
        var specialty = specialtyRepository.save(Specialty.builder().name("Cardiología").build());

        // When
        boolean exists = specialtyRepository.existsByName(specialty.getName());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Specialty: No detecta la existencia de una especialidad si el nombre no coincide")
    void shouldReturnFalseWhenNameDoesNotMatchForExistsByName() {
        // Given
        specialtyRepository.save(Specialty.builder().name("Cardiología").build());

        // When
        boolean exists = specialtyRepository.existsByName("Pediatría");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Specialty: Encuentra una especialidad por su nombre")
    void shouldFindByName() {
        // Given
        var specialty = specialtyRepository.save(Specialty.builder().name("Cardiología").build());

        // When
        var found = specialtyRepository.findByName(specialty.getName());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(specialty.getId());
        assertThat(found.get().getName()).isEqualTo(specialty.getName());
    }

    @Test
    @DisplayName("Specialty: No encuentra una especialidad si el nombre no coincide")
    void shouldNotFindByName() {
        // Given
        specialtyRepository.save(Specialty.builder().name("Cardiología").build());

        // When
        var found = specialtyRepository.findByName("Pediatría");

        // Then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("Specialty: Cuenta la cantidad de citas canceladas y no asistidas agrupadas por especialidad")
    void shouldCountCancelledAndNoShowBySpecialty() {
        // Given
        setUp();
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

        var specialty2 = specialtyRepository.save(Specialty.builder().name("Psicología").build());

        var doctor2 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dra. Smith")
                .documentNumber("654321")
                .licenseNumber("LIC-002")
                .email("drasmith@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty2)
                .createdAt(Instant.now())
                .build()
        );

        // Medicina General - tendrá 1 CANCELLED y 1 NO_SHOW
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime)
            .endAt(baseDateTime.plusMinutes(30))
            .status(AppointmentStatus.CANCELLED)
            .createdAt(Instant.now())
            .build()
        );
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(1))
            .endAt(baseDateTime.plusDays(1).plusMinutes(30))
            .status(AppointmentStatus.NO_SHOW)
            .createdAt(Instant.now())
            .build()
        );

        // Psicología - tendrá 1 NO_SHOW y 1 CONFIRMED (que no debe contarse)
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor2)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(2))
            .endAt(baseDateTime.plusDays(2).plusMinutes(30))
            .status(AppointmentStatus.NO_SHOW)
            .createdAt(Instant.now())
            .build()
        );
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor2)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(3))
            .endAt(baseDateTime.plusDays(3).plusMinutes(30))
            .status(AppointmentStatus.CONFIRMED)
            .createdAt(Instant.now())
            .build()
        );

        // When
        var stats = specialtyRepository.countCancelledAndNoShowBySpecialty();

        // Then
        assertThat(stats).hasSize(2);
        assertThat(stats).extracting(s -> s.getSpecialtyId())
            .containsExactly(specialty.getId(), specialty2.getId());
        assertThat(stats).extracting(s -> s.getSpecialtyName())
            .containsExactly("Medicina General", "Psicología");
        assertThat(stats).extracting(s -> s.getCancelledCount())
            .containsExactly(1L, 0L); 
        assertThat(stats).extracting(s -> s.getNoShowCount())
            .containsOnly(1L);
    }

    @Test
    @DisplayName("Specialty: No cuenta citas con estados distintos a canceladas o no asistidas")
    void shouldNotCountNonCancelledOrNoShowForCountCancelledAndNoShowBySpecialty() {
        // Given
        setUp();
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime)
            .endAt(baseDateTime.plusMinutes(30))
            .status(AppointmentStatus.CONFIRMED)
            .createdAt(Instant.now())
            .build()
        );
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(1))
            .endAt(baseDateTime.plusDays(1).plusMinutes(30))
            .status(AppointmentStatus.SCHEDULED)
            .createdAt(Instant.now())
            .build()
        );
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(2))
            .endAt(baseDateTime.plusDays(2).plusMinutes(30))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now())
            .build()
        );

        // When
        var stats = specialtyRepository.countCancelledAndNoShowBySpecialty();

        // Then
        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getSpecialtyId()).isEqualTo(specialty.getId());
        assertThat(stats.get(0).getSpecialtyName()).isEqualTo("Medicina General");
        assertThat(stats.get(0).getCancelledCount()).isEqualTo(0L);
        assertThat(stats.get(0).getNoShowCount()).isEqualTo(0L);        
    }
    
    @Test
    @DisplayName("Specialty: Cuenta especialidades sin citas como 0 canceladas y 0 no asistidas")
    void shouldCountSpecialtiesWithNoAppointmentsAsZeroForCountCancelledAndNoShowBySpecialty() {
        // Given
        var specialty1 = specialtyRepository.save(Specialty.builder().name("Dermatología").build());

        // When
        var stats = specialtyRepository.countCancelledAndNoShowBySpecialty();

        // Then
        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getSpecialtyId()).isEqualTo(specialty1.getId());
        assertThat(stats.get(0).getSpecialtyName()).isEqualTo("Dermatología");
        assertThat(stats.get(0).getCancelledCount()).isEqualTo(0L);
        assertThat(stats.get(0).getNoShowCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Specialty: Cuenta correctamente cuando hay múltiples doctores con la misma especialidad")
    void shouldCountMultipleDoctorsWithSameSpecialtyForCountCancelledAndNoShowBySpecialty() {
        // Given
        setUp();
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

        var doctor2 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dra. Smith")
                .documentNumber("654321")
                .licenseNumber("LIC-002")
                .email("drasmith@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );

        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime)
            .endAt(baseDateTime.plusMinutes(30))
            .status(AppointmentStatus.CANCELLED)
            .createdAt(Instant.now())
            .build()
        );
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor2)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(1))
            .endAt(baseDateTime.plusDays(1).plusMinutes(30))
            .status(AppointmentStatus.NO_SHOW)
            .createdAt(Instant.now())
            .build()
        );

        // When
        var stats = specialtyRepository.countCancelledAndNoShowBySpecialty();

        // Then
        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getSpecialtyId()).isEqualTo(specialty.getId());
        assertThat(stats.get(0).getSpecialtyName()).isEqualTo("Medicina General");
        assertThat(stats.get(0).getCancelledCount()).isEqualTo(1L);
        assertThat(stats.get(0).getNoShowCount()).isEqualTo(1L);
    }
}
