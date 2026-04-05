package com.githubzs.plataforma_reservas_medicas.Repositories;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    @DisplayName("Debe encontrar un paciente por número de documento")
    void shouldFindByDocumentNumber() {
        // Given
        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        // When
        var found = patientRepository.findByDocumentNumber("123456");
        var notFound = patientRepository.findByDocumentNumber("999999");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(patient.getId());
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
        assertThat(found.get().getDocumentNumber()).isEqualTo("123456");
        assertThat(found.get().getEmail()).isEqualTo("johndoe@gmail.com");
        assertThat(found.get().getStatus()).isEqualTo(PatientStatus.ACTIVE);

        // Verificación inversa
        assertThat(notFound).isNotPresent();
    }

    @Test
    @DisplayName("Debe contar inasistencias de un paciente en un rango de fechas")
    void shouldCountNoShowByPatient() {
        // Given
        var now = LocalDateTime.now();
        var from = now.minusDays(1);
        var to = now.plusDays(1);

        var patient = patientRepository.save(buildDefaultPatient("John Doe", "123456", "johndoe@gmail.com"));

        // 2 NO_SHOW dentro del rango
        appointmentRepository.save(buildDefaultAppointment(patient, now, AppointmentStatus.NO_SHOW));
        appointmentRepository.save(buildDefaultAppointment(patient, now.plusHours(1), AppointmentStatus.NO_SHOW));

        // CONFIRMED dentro del rango - no debe contar
        appointmentRepository.save(buildDefaultAppointment(patient, now.plusHours(2), AppointmentStatus.CONFIRMED));

        // NO_SHOW fuera del rango - no debe contar
        appointmentRepository.save(buildDefaultAppointment(patient, now.plusDays(5), AppointmentStatus.NO_SHOW));

        // When
        var count = patientRepository.countNoShowByPatient(patient.getId(), from, to);
        var countOutOfRange = patientRepository.countNoShowByPatient(
            patient.getId(), now.plusDays(2), now.plusDays(10)
        );
        var countDifferentPatient = patientRepository.countNoShowByPatient(
            UUID.randomUUID(), from, to
        );

        // Then
        assertThat(count).isEqualTo(2);

        // Verificación inversa - CONFIRMED no se cuenta
        // Verificación inversa - NO_SHOW fuera del rango no se cuenta
        assertThat(countOutOfRange).isEqualTo(1); // solo el plusDays(5)
        
        // Verificación inversa - paciente diferente no tiene inasistencias
        assertThat(countDifferentPatient).isEqualTo(0);
    }

    @Test
    @DisplayName("Debe retornar ranking de pacientes con más inasistencias en un rango de fechas")
    void shouldCountPatientsNoShow() {
        // Given
        var now = LocalDateTime.now();
        var from = now.minusDays(1);
        var to = now.plusDays(1);

        // Patient1 - 2 NO_SHOW → debe quedar primero
        var patient1 = patientRepository.save(buildDefaultPatient("John Doe", "111111", "johndoe@gmail.com"));

        // Patient2 - 1 NO_SHOW → debe quedar segundo
        var patient2 = patientRepository.save(buildDefaultPatient("Jane Smith", "222222", "janesmith@gmail.com"));

        // Patient3 - solo CONFIRMED → no debe aparecer en el resultado
        var patient3 = patientRepository.save(buildDefaultPatient("Bob Martin", "333333", "bobmartin@gmail.com"));

        // 2 NO_SHOW para patient1
        appointmentRepository.save(buildDefaultAppointment(patient1, now, AppointmentStatus.NO_SHOW));
        appointmentRepository.save(buildDefaultAppointment(patient1, now.plusHours(1), AppointmentStatus.NO_SHOW));

        // 1 NO_SHOW para patient2
        appointmentRepository.save(buildDefaultAppointment(patient2, now, AppointmentStatus.NO_SHOW));

        // CONFIRMED para patient1 - no debe contar
        appointmentRepository.save(buildDefaultAppointment(patient1, now.plusHours(2), AppointmentStatus.CONFIRMED));

        // NO_SHOW fuera del rango para patient2 - no debe contar
        appointmentRepository.save(buildDefaultAppointment(patient2, now.plusDays(5), AppointmentStatus.NO_SHOW));

        // Solo CONFIRMED para patient3 - no debe aparecer
        appointmentRepository.save(buildDefaultAppointment(patient3, now, AppointmentStatus.CONFIRMED));

        // When
        var result = patientRepository.countPatientsNoShow(from, to);

        // Then
        assertThat(result).hasSize(2);

        // Verifica orden descendente
        var stat1 = result.get(0);
        assertThat(stat1.getPatientId()).isEqualTo(patient1.getId());
        assertThat(stat1.getPatientName()).isEqualTo("John Doe");
        assertThat(stat1.getNoShowCount()).isEqualTo(2);

        var stat2 = result.get(1);
        assertThat(stat2.getPatientId()).isEqualTo(patient2.getId());
        assertThat(stat2.getPatientName()).isEqualTo("Jane Smith");
        assertThat(stat2.getNoShowCount()).isEqualTo(1);

        // Verificación inversa - patient3 no aparece (solo tiene CONFIRMED)
        assertThat(result).extracting(PatientNoShowStatsDto::getPatientId)
            .doesNotContain(patient3.getId());

        // Verificación inversa - orden descendente correcto
        assertThat(result).extracting(PatientNoShowStatsDto::getNoShowCount)
            .isSortedAccordingTo(Comparator.reverseOrder());

        // Verificación inversa - NO_SHOW fuera del rango no afectó el conteo de patient2
        assertThat(stat2.getNoShowCount()).isEqualTo(1);
    }
}