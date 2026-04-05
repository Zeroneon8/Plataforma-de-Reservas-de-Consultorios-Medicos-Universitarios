package com.githubzs.plataforma_reservas_medicas.Repositories;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.githubzs.plataforma_reservas_medicas.domine.dto.PatientNoShowStatsDto;
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

public class PatientRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private OfficeRepository officeRepository;
    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired
    private SpecialtyRepository specialtyRepository;

    // Datos base utilizados en todos los tests
    private Doctor doctor;
    private Office office;
    private AppointmentType appointmentType;

    @BeforeEach
    void setUp() {
        var specialty = specialtyRepository.save(
            Specialty.builder()
                .name("Medicina General")
                .build()
        );

        doctor = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. House")
                .documentNumber("111111")
                .licenseNumber("LIC-001")
                .email("drhouse@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );

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
    }

    private Patient buildDefaultPatient(String fullName, String documentNumber, String email) {
        return Patient.builder()
            .fullName(fullName)
            .documentNumber(documentNumber)
            .phoneNumber("324-123-4567")
            .email(email)
            .createdAt(Instant.now())
            .status(PatientStatus.ACTIVE)
            .build();
    }

    private Appointment buildDefaultAppointment(Patient patient, LocalDateTime start, AppointmentStatus status) {
        return Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(start)
            .endAt(start.plusMinutes(30))
            .status(status)
            .createdAt(Instant.now())
            .build();
    }

    @Test
    @DisplayName("Patient: Detecta si existe un paciente especifico con un estado dado")
    void shouldExistByIdAndStatus() {
        // Given
        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        // When
        var exist = patientRepository.existsByIdAndStatus(patient.getId(), PatientStatus.ACTIVE);

        // Then
        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("Patient: No detecta la existencia de un paciente si el id coincide pero el estado no")
    void shouldReturnFalseWhenStatusDoesNotMatchForExistByIdAndStatus() {
        // Given
        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        // When
        var exist = patientRepository.existsByIdAndStatus(patient.getId(), PatientStatus.INACTIVE);

        // Then
        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Patient: No detecta la existencia de un paciente si el estado coincide pero el id no")
    void shouldReturnFalseWhenIdDoesNotMatchForExistByIdAndStatus() {
        // Given
        var patient1 = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));
        var altId = new UUID(patient1.getId().getMostSignificantBits(), patient1.getId().getLeastSignificantBits() + 1);

        // When
        var exist = patientRepository.existsByIdAndStatus(altId, PatientStatus.ACTIVE);

        // Then
        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Patient: Encuentra un paciente por número de documento")
    void shouldFindByDocumentNumber() {
        // Given
        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        // When
        var found = patientRepository.findByDocumentNumber("123456");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(patient.getId());
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
        assertThat(found.get().getDocumentNumber()).isEqualTo("123456");
        assertThat(found.get().getEmail()).isEqualTo("johndoe@gmail.com");
        assertThat(found.get().getStatus()).isEqualTo(PatientStatus.ACTIVE);
    }

    @Test
    @DisplayName("Patient: No encuentra un paciente por número de documento no presente en la base de datos")
    void shouldReturnEmptyWhenDocumentNumberDoesNotMatchForFindByDocumentNumber() {
        // When
        var found = patientRepository.findByDocumentNumber("999999");

        // Then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("Patient: Cuenta inasistencias (NO_SHOW) de un paciente en un rango de fechas")
    void shouldCountNoShowByPatient() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime, AppointmentStatus.NO_SHOW));
        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime.plusHours(1), AppointmentStatus.NO_SHOW));
        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime.plusHours(2), AppointmentStatus.CONFIRMED)); // no debe contar porque no es NO_SHOW
        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime.plusDays(5), AppointmentStatus.NO_SHOW)); // no debe contar porque está fuera del rango

        // When
        var count = patientRepository.countNoShowByPatient(patient.getId(), from, to);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Patient: Incluye citas NO_SHOW en los extremos del rango de fechas al contar inasistencias")
    void shouldIncludeNoShowFromAndToForCountNoShowByPatient() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime;
        var to = baseDateTime.plusHours(2);

        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime, AppointmentStatus.NO_SHOW)); 
        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime.plusHours(2), AppointmentStatus.NO_SHOW)); 
        
        // When
        var count = patientRepository.countNoShowByPatient(patient.getId(), from, to);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Patient: No cuenta inasistencias (NO_SHOW) de un paciente fuera del rango de fechas")
    void shouldReturnZeroWhenNoShowIsOutsideDateRange() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime.minusDays(2), AppointmentStatus.NO_SHOW)); 
        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime.plusDays(2), AppointmentStatus.NO_SHOW)); 

        // When
        var count = patientRepository.countNoShowByPatient(patient.getId(), from, to);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Patient: No cuenta inasistencias (NO_SHOW) dentro del rango de fechas si el id de paciente no coincide")
    void shouldReturnZeroWhenPatientIdDoesNotMatchForCountNoShowByPatient() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusHours(2);
        var to = baseDateTime.plusHours(2);

        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        var altId = new UUID(patient.getId().getMostSignificantBits(), patient.getId().getLeastSignificantBits() + 1);

        appointmentRepository.save(buildDefaultAppointment(patient, baseDateTime, AppointmentStatus.NO_SHOW)); 
        
        // When
        var count = patientRepository.countNoShowByPatient(altId, from, to);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Patient: Lista pacientes ordenados por cantidad de inasistencias (NO_SHOW) en un rango de fechas")
    void shouldCountPatientsNoShow() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        var patient1 = patientRepository.save(buildDefaultPatient("John Doe", "111111", "johndoe@gmail.com"));
        var patient2 = patientRepository.save(buildDefaultPatient("Jane Smith", "222222", "janesmith@gmail.com"));
        var patient3 = patientRepository.save(buildDefaultPatient("Bob Martin", "333333", "bobmartin@gmail.com"));

        // Paciente 1 - 2 citas NO_SHOW
        appointmentRepository.save(buildDefaultAppointment(patient1, baseDateTime, AppointmentStatus.NO_SHOW));
        appointmentRepository.save(buildDefaultAppointment(patient1, baseDateTime.plusHours(1), AppointmentStatus.NO_SHOW));

        // Paciente 2 - 1 cita NO_SHOW y 1 CONFIRMED (solo NO_SHOW debe contar)
        appointmentRepository.save(buildDefaultAppointment(patient2, baseDateTime, AppointmentStatus.NO_SHOW));
        appointmentRepository.save(buildDefaultAppointment(patient2, baseDateTime.plusHours(1), AppointmentStatus.CONFIRMED));

        // Paciente 3 - sin citas NO_SHOW, no debe aparecer en el resultado
        appointmentRepository.save(buildDefaultAppointment(patient3, baseDateTime, AppointmentStatus.CONFIRMED));

        // When
        var result = patientRepository.countPatientsNoShow(from, to);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PatientNoShowStatsDto::getPatientId)
            .containsExactly(patient1.getId(), patient2.getId());
        assertThat(result).extracting(PatientNoShowStatsDto::getPatientName)
            .containsExactly("John Doe", "Jane Smith");
        assertThat(result).extracting(PatientNoShowStatsDto::getNoShowCount)
            .containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("Patient: Lista pacientes con el mismo número de inasistencias (NO_SHOW) en un rango de fechas orden alfabético del nombre")
    void shouldOrderPatientsWithSameNoShowCountByName() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        var patient1 = patientRepository.save(buildDefaultPatient("John Doe", "111111", "johndoe@gmail.com"));
        var patient2 = patientRepository.save(buildDefaultPatient("Aston Martin", "222222", "vulcan@gmail.com"));

        // Ambos pacientes tienen 1 cita NO_SHOW
        appointmentRepository.save(buildDefaultAppointment(patient1, baseDateTime, AppointmentStatus.NO_SHOW));
        appointmentRepository.save(buildDefaultAppointment(patient2, baseDateTime, AppointmentStatus.NO_SHOW));

        // When
        var result = patientRepository.countPatientsNoShow(from, to);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PatientNoShowStatsDto::getPatientId)
            .containsExactly(patient2.getId(), patient1.getId());
        assertThat(result).extracting(PatientNoShowStatsDto::getPatientName)
            .containsExactly("Aston Martin", "John Doe");
        assertThat(result).extracting(PatientNoShowStatsDto::getNoShowCount)
            .containsOnly(1L);
    }

    @Test
    @DisplayName("Patient: No cuenta citas con estado distinto a NO_SHOW al listar pacientes por cantidad de inasistencias")
    void shouldReturnEmptyWhenNoNoShowAppointmentsForCountPatientsNoShow() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        var patient1 = patientRepository.save(buildDefaultPatient("John Doe", "111111", "johndoe@gmail.com"));
    
        appointmentRepository.save(buildDefaultAppointment(patient1, baseDateTime, AppointmentStatus.COMPLETED));
        appointmentRepository.save(buildDefaultAppointment(patient1, baseDateTime.plusHours(1), AppointmentStatus.CANCELLED));
        appointmentRepository.save(buildDefaultAppointment(patient1, baseDateTime.plusHours(2), AppointmentStatus.SCHEDULED));
        appointmentRepository.save(buildDefaultAppointment(patient1, baseDateTime.plusHours(3), AppointmentStatus.CONFIRMED));

        // When
        var result = patientRepository.countPatientsNoShow(from, to);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Patient: Listado de pacientes por cantidad de inasistencias (NO_SHOW) en un rango de fechas sin pacientes devuelve lista vacía")
    void shouldReturnEmptyWhenNoPatientsForCountPatientsNoShow() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        // When
        var result = patientRepository.countPatientsNoShow(from, to);

        // Then
        assertThat(result).isEmpty();
    }
}