package com.githubzs.plataforma_reservas_medicas.Repositories;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.domine.dto.DoctorRankingStatsDto;
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

class DoctorRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private SpecialtyRepository specialtyRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private OfficeRepository officeRepository;
    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired
    private PatientRepository patientRepository;
    
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
    @DisplayName("Doctor: Encuentra un doctor por su número de documento")
    void shouldFindByDocumentNumber() {
        // Given - ya creado en el setUp()

        // When
        var found = doctorRepository.findByDocumentNumber("123456");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(doctor.getId());
        assertThat(found.get().getFullName()).isEqualTo("Dr. House");
        assertThat(found.get().getDocumentNumber()).isEqualTo("123456");
        assertThat(found.get().getEmail()).isEqualTo("drhouse@doctor.com");
        assertThat(found.get().getStatus()).isEqualTo(DoctorStatus.ACTIVE);
        assertThat(found.get().getSpecialty().getId()).isEqualTo(specialty.getId());
    }

    @Test
    @DisplayName("Doctor: No encuentra un doctor con número de documento no presente en la base de datos")
    void shouldNotFindByDocumentNumber() {
        // Given - ya creado en el setUp()

        // When
        var found = doctorRepository.findByDocumentNumber("999999");

        // Then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("Doctor: Encuentra doctores por especialidad")
    void shouldFindBySpecialtyId() {
        // Given
        var specialty2 = specialtyRepository.save(
            Specialty.builder()
                .name("Psicología")
                .build()
        );

        var doctor2 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dra. Grey")
                .documentNumber("222222")
                .licenseNumber("LIC-002")
                .email("drgrey@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty2)
                .createdAt(Instant.now())
                .build()
        );

        // When
        var found = doctorRepository.findBySpecialty_Id(specialty2.getId(), Pageable.ofSize(10));

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(Doctor::getId)
            .containsExactly(doctor2.getId());
        assertThat(found).extracting(Doctor::getSpecialty)
            .extracting(Specialty::getId)
            .containsExactly(specialty2.getId());
    }

    @Test
    @DisplayName("Doctor: No encuentra doctores cuando la especialidad no coincide")
    void shouldReturnEmptyWhenSpecialtyDoesNotMatchForFindBySpecialtyId() {
        // Given
        var altId = new UUID(specialty.getId().getMostSignificantBits(), specialty.getId().getLeastSignificantBits() + 1);

        // When
        var found = doctorRepository.findBySpecialty_Id(altId, Pageable.ofSize(10));

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Doctor: Encuentra doctores por especialidad y estado")
    void shouldFindByStatusAndSpecialtyId() {
        // Given
        var specialty2 = specialtyRepository.save(
            Specialty.builder()
                .name("Psicología")
                .build()
        );

        doctorRepository.save(
            Doctor.builder()
                .fullName("Dra. Grey")
                .documentNumber("222222")
                .licenseNumber("LIC-002")
                .email("drgrey@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty2)
                .createdAt(Instant.now())
                .build()
        );

        doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. Wilson")
                .documentNumber("333333")
                .licenseNumber("LIC-003")
                .email("drwilson@doctor.com")
                .status(DoctorStatus.INACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );

        doctorRepository.save(
            Doctor.builder()
                .fullName("Dra. Adams")
                .documentNumber("444444")
                .licenseNumber("LIC-004")
                .email("draadams@doctor.com")
                .status(DoctorStatus.INACTIVE)
                .specialty(specialty2)
                .createdAt(Instant.now())
                .build()
        );

        // When
        var found = doctorRepository.findByStatusAndSpecialty_Id(
            DoctorStatus.ACTIVE, specialty.getId(), Pageable.ofSize(10)
        );

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(Doctor::getId)
            .containsExactly(doctor.getId());
        assertThat(found).extracting(Doctor::getStatus)
            .containsOnly(DoctorStatus.ACTIVE);
        assertThat(found).extracting(Doctor::getSpecialty)
            .extracting(Specialty::getId)
            .containsOnly(specialty.getId());
    }

    @Test
    @DisplayName("Doctor: No encuentra doctores cuando la especialidad coincide pero el estado no")
    void shouldReturnEmptyWhenStatusDoesNotMatchForFindByStatusAndSpecialtyId() {
        // Given - ya creado en el setUp()

        // When
        var found = doctorRepository.findByStatusAndSpecialty_Id(
            DoctorStatus.INACTIVE, specialty.getId(), Pageable.ofSize(10)
        );

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Doctor: No encuentra doctores cuando el estado coincide pero la especialidad no")
    void shouldReturnEmptyWhenSpecialtyDoesNotMatchForFindByStatusAndSpecialtyId() {
        // Given
        var specialty2 = specialtyRepository.save(
            Specialty.builder()
                .name("Psicología")
                .build()
        );

        // When
        var found = doctorRepository.findByStatusAndSpecialty_Id(
            DoctorStatus.ACTIVE, specialty2.getId(), Pageable.ofSize(10)
        );

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Doctor: Detecta si existe un doctor por estado")
    void shouldExistsByIdAndStatus() {
        // Given - ya creado en el setUp()

        // When
        var exists = doctorRepository.existsByIdAndStatus(
            doctor.getId(), DoctorStatus.ACTIVE
        );

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Doctor: No detecta la existencia de un doctor si el id coincide pero el estado no")
    void shouldReturnFalseWhenStatusDoesNotMatchForExistsByIdAndStatus() {
        // Given - ya creado en el setUp()

        // When
        var exists = doctorRepository.existsByIdAndStatus(
            doctor.getId(), DoctorStatus.INACTIVE
        );

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Doctor: No detecta la existencia de un doctor si el estado coincide pero el ID no")
    void shouldReturnFalseWhenIdDoesNotMatchForExistsByIdAndStatus() {
        // Given
        var altId = new UUID(doctor.getId().getMostSignificantBits(), doctor.getId().getLeastSignificantBits() + 1);

        // When
        var exists = doctorRepository.existsByIdAndStatus(
            altId, DoctorStatus.ACTIVE
        );

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Doctor: Detecta si existe un doctor por número de documento")
    void shouldExistsByDocumentNumber() {
        // Given - ya creado en el setUp()

        // When
        var exists = doctorRepository.existsByDocumentNumber("123456");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Doctor: No detecta la existencia de un doctor con número de documento no registrado")
    void shouldReturnFalseWhenDocumentNumberDoesNotExist() {
        // Given - ya creado en el setUp()

        // When
        var exists = doctorRepository.existsByDocumentNumber("999999");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Doctor: Detecta si existe un doctor por número de licencia")
    void shouldExistsByLicenseNumber() {
        // Given - ya creado en el setUp()

        // When
        var exists = doctorRepository.existsByLicenseNumber("LIC-001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Doctor: No detecta la existencia de un doctor con número de licencia no registrado")
    void shouldReturnFalseWhenLicenseNumberDoesNotExist() {
        // Given - ya creado en el setUp()

        // When
        var exists = doctorRepository.existsByLicenseNumber("LIC-999");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Doctor: Detecta si existe un doctor por email (sin importar mayúsculas)")
    void shouldExistsByEmailIgnoreCase() {
        // Given - ya creado en el setUp()

        // When
        var existsLowerCase = doctorRepository.existsByEmailIgnoreCase("drhouse@doctor.com");
        var existsUpperCase = doctorRepository.existsByEmailIgnoreCase("DRHOUSE@DOCTOR.COM");
        var existsMixedCase = doctorRepository.existsByEmailIgnoreCase("DrHouse@Doctor.Com");

        // Then
        assertThat(existsLowerCase).isTrue();
        assertThat(existsUpperCase).isTrue();
        assertThat(existsMixedCase).isTrue();
    }

    @Test
    @DisplayName("Doctor: No detecta la existencia de un doctor con email no registrado")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // Given - ya creado en el setUp()

        // When
        var exists = doctorRepository.existsByEmailIgnoreCase("unknown@doctor.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Doctor: rankea doctores por cantidad de citas completadas en orden descendente")
    void shouldRankDoctorsByCompletedAppointments() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

        var doctor2 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dra. Grey")
                .documentNumber("222222")
                .licenseNumber("LIC-002")
                .email("drgrey@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );
        var doctor3 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. Wilson")
                .documentNumber("333333")
                .licenseNumber("LIC-003")
                .email("drwilson@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );

        var patient = patientRepository.save(
            Patient.builder()
                .fullName("John Doe")
                .documentNumber("123456")
                .phoneNumber("3241234567")
                .email("johndoe@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );

        var office = officeRepository.save(
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

        // Doctor 1 - tendrá 2 citas completadas, debe quedar primero
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime).endAt(baseDateTime.plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now()).build()
        );
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(1)).endAt(baseDateTime.plusDays(1).plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now()).build()
        );

        // Doctor 2 - tendrá 1 cita completada y 1 cancelada, deber quedar segundo con 1 (la cancelada no debe contar)
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor2).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(2)).endAt(baseDateTime.plusDays(2).plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.CANCELLED)
            .createdAt(Instant.now()).build()
        );
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor2).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(3)).endAt(baseDateTime.plusDays(3).plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now()).build()
        );

        // Doctor 3 - sin citas completadas, debe quedar último con 0

        // When
        var result = doctorRepository.rankDoctorsByCompletedAppointments();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(DoctorRankingStatsDto::getDoctorId)
            .containsExactly(doctor.getId(), doctor2.getId(), doctor3.getId());
        assertThat(result).extracting(DoctorRankingStatsDto::getCompletedAppointments)
            .containsExactly(2L, 1L, 0L);
        assertThat(result).extracting(DoctorRankingStatsDto::getDoctorName)
            .containsExactly("Dr. House", "Dra. Grey", "Dr. Wilson");
    }

    @Test
    @DisplayName("Doctor: rankea doctores con mismo número de citas completadas por orden alfabético del nombre")
    void shouldRankDoctorsWithSameCompletedAppointmentsByName() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

        var doctor2 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. Adams") 
                .documentNumber("122345")
                .licenseNumber("LIC-002")
                .email("adamsdr@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );
        
        var patient = patientRepository.save(
            Patient.builder()
                .fullName("John Doe")
                .documentNumber("123456")
                .phoneNumber("3241234567")
                .email("johndoe@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );

        var office = officeRepository.save(
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

        // Ambos doctores tienen 1 cita completada
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime).endAt(baseDateTime.plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now()).build()
        );

        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor2).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(1)).endAt(baseDateTime.plusDays(1).plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now()).build()
        );

        // When
        var result = doctorRepository.rankDoctorsByCompletedAppointments();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(DoctorRankingStatsDto::getDoctorId)
            .containsExactly(doctor2.getId(), doctor.getId());
        assertThat(result).extracting(DoctorRankingStatsDto::getCompletedAppointments)
            .containsOnly(1L);
        assertThat(result).extracting(DoctorRankingStatsDto::getDoctorName)
            .containsExactly("Dr. Adams", "Dr. House");
    }

    @Test
    @DisplayName("Doctor: no cuenta citas que no estén en estado COMPLETED para el ranking")
    void shouldNotCountNonCompletedAppointmentsForRanking() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

        var patient = patientRepository.save(
            Patient.builder()
                .fullName("John Doe")
                .documentNumber("123456")
                .phoneNumber("3241234567")
                .email("johndoe@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );

        var office = officeRepository.save(
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

        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime).endAt(baseDateTime.plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.SCHEDULED) 
            .createdAt(Instant.now()).build()
        );

        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(1)).endAt(baseDateTime.plusDays(1).plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.CANCELLED) 
            .createdAt(Instant.now()).build()
        );

        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(2)).endAt(baseDateTime.plusDays(2).plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.NO_SHOW) 
            .createdAt(Instant.now()).build()
        );

        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor).office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusDays(3)).endAt(baseDateTime.plusDays(3).plusMinutes(appointmentType.getDurationMinutes()))
            .status(AppointmentStatus.CONFIRMED) 
            .createdAt(Instant.now()).build()
        );

        // When
        var result = doctorRepository.rankDoctorsByCompletedAppointments();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).extracting(DoctorRankingStatsDto::getDoctorId)
            .containsExactly(doctor.getId());
        assertThat(result).extracting(DoctorRankingStatsDto::getCompletedAppointments)
            .containsExactly(0L);
        assertThat(result).extracting(DoctorRankingStatsDto::getDoctorName)
            .containsExactly("Dr. House");
    }

    @Test
    @DisplayName("Doctor: ranking de doctores sin doctores registrados devuelve lista vacía")
    void shouldReturnEmptyWhenNoDoctorsForRanking() {
        // Given
        doctorRepository.deleteAll();

        // When
        var result = doctorRepository.rankDoctorsByCompletedAppointments();

        // Then
        assertThat(result).isEmpty();
    }
    
}
