package com.githubzs.plataforma_reservas_medicas.Repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

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

public class AppointmentRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private OfficeRepository officeRepository;
    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired
    private SpecialtyRepository specialtyRepository;

    // Datos base reutilizados en todos los tests
    private Patient patient;
    private Doctor doctor;
    private Office office;
    private AppointmentType appointmentType;
    
    // Fecha y hora base para todos los test
    private LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

    // BeforeEach asegura que en cada test partimos de un estado limpio y consistente, evitando que los tests se afecten entre sí
    @BeforeEach
    void setUp() {
        var specialty = specialtyRepository.save(
            Specialty.builder()
                .name("Medicina General")
                .build()
        );

        patient = patientRepository.save(
            Patient.builder()
                .fullName("John Doe")
                .documentNumber("123456")
                .phoneNumber("3241234567")
                .email("johndoe@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );

        doctor = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr.House")
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

    // Helper para no repetir el builder con valores por defecto en cada test
    private Appointment buildDefaultAppointment(LocalDateTime start, AppointmentStatus status) {
        return Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(start)
            .endAt(start.plusMinutes(appointmentType.getDurationMinutes()))
            .status(status)
            .createdAt(Instant.now())
            .build();
    }

    @Test
    @DisplayName("Appointment: Encuentra citas de un paciente por el estado indicado")
    void shouldFindByPatientIdAndStatus() {
        // Given
        var patient2 = patientRepository.save(
            Patient.builder()
                .fullName("Jane Doe")
                .documentNumber("987654")
                .phoneNumber("3019283728")
                .email("janedoe@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );
        var appointment1 = appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildDefaultAppointment(baseDateTime.plusHours(1), AppointmentStatus.CONFIRMED));
        appointmentRepository.save(buildDefaultAppointment(baseDateTime.plusHours(2), AppointmentStatus.CANCELLED));
        appointmentRepository.save(
            Appointment.builder()
                .patient(patient2)
                .doctor(doctor)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(baseDateTime.plusHours(3))
                .endAt(baseDateTime.plusHours(3).plusMinutes(appointmentType.getDurationMinutes()))
                .status(AppointmentStatus.CONFIRMED)
                .createdAt(Instant.now())
                .build()
        );

        // When
        var found = appointmentRepository.findByPatient_IdAndStatus(
            patient.getId(), AppointmentStatus.CONFIRMED, Pageable.ofSize(10)
        );

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Appointment::getId)
            .containsExactlyInAnyOrder(appointment1.getId(), appointment2.getId());
        assertThat(found).extracting(Appointment::getStatus)
            .containsOnly(AppointmentStatus.CONFIRMED);
        assertThat(found).extracting(a -> a.getPatient().getId())
            .containsOnly(patient.getId());
    }

    @Test
    @DisplayName("Appointment: No devuelve citas cuando el paciente coincide pero el estado no")
    void shouldReturnEmptyWhenStatusDoesNotMatchForFindByPatientIdAndStatus() {
        // Given
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CANCELLED));
        
        // When
        var found = appointmentRepository.findByPatient_IdAndStatus(
            patient.getId(), AppointmentStatus.CONFIRMED, Pageable.ofSize(10)
        );

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Appointment: No devuelve citas cuando el estado coincide pero el paciente no")
    void shouldReturnEmptyWhenPatientDoesNotMatchForFindByPatientIdAndStatus() {
        // Given
        var patient2 = patientRepository.save(
            Patient.builder()
                .fullName("Jane Doe")
                .documentNumber("987654")
                .phoneNumber("3019283728")
                .email("janedoe@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));
        
        // When
        var found = appointmentRepository.findByPatient_IdAndStatus(
            patient2.getId(), AppointmentStatus.CONFIRMED, Pageable.ofSize(10)
        );

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Appointment: Encuentra citas por el rango de fechas indicado")
    void shouldFindByStartAtBetween() {
        // Given
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        var appointment1 = appointmentRepository.save(buildDefaultAppointment(baseDateTime.minusDays(1), AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildDefaultAppointment(baseDateTime.plusHours(1), AppointmentStatus.CANCELLED));

        // Citas fuera del rango - no deben aparecer
        appointmentRepository.save(buildDefaultAppointment(baseDateTime.minusDays(5), AppointmentStatus.CONFIRMED));
        appointmentRepository.save(buildDefaultAppointment(baseDateTime.plusDays(5), AppointmentStatus.CONFIRMED));
        
        // When
        var found = appointmentRepository.findByStartAtBetween(from, to, Pageable.ofSize(10));

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Appointment::getId)
            .containsExactlyInAnyOrder(appointment1.getId(), appointment2.getId());
        assertThat(found).extracting(Appointment::getStartAt)
            .allMatch(start -> !start.isBefore(from) && !start.isAfter(to));
    }
    
    @Test
    @DisplayName("Appointment: Encuentra citas por el rango de fehchas indicado contando bordes (From y To)")
    void shouldIncludeFromAndToForFindByStartAtBetween() {
        // Given
        var from = baseDateTime;
        var to = baseDateTime.plusHours(1);

        var appointment1 = appointmentRepository.save(buildDefaultAppointment(from, AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildDefaultAppointment(to, AppointmentStatus.CONFIRMED));

        // When
        var found = appointmentRepository.findByStartAtBetween(from, to, Pageable.ofSize(10));

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Appointment::getId)
            .containsExactlyInAnyOrder(appointment1.getId(), appointment2.getId());
        assertThat(found).extracting(Appointment::getStartAt)
            .allMatch(start -> !start.isBefore(from) && !start.isAfter(to));
    }

    @Test
    @DisplayName("Appointment: No devuelve citas cuando el rango de fechas no coincide")
    void shouldReturnEmptyWhenNoAppointmentsInRangeForFindByStartAtBetween() {
        // Given
        var from = baseDateTime.minusDays(5);
        var to = baseDateTime.minusDays(4);

        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));

        // When
        var found = appointmentRepository.findByStartAtBetween(from, to, Pageable.ofSize(10));

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Appointment: Encuentra una cita por su ID y estado")
    void shouldFindByIdAndStatus() {
        // Given
        var appointment1 = appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CANCELLED));

        // When
        var found = appointmentRepository.findByIdAndStatus(appointment1.getId(), AppointmentStatus.CONFIRMED);
        var notFoundWrongStatus = appointmentRepository.findByIdAndStatus(appointment1.getId(), AppointmentStatus.CANCELLED);
        var notFoundWrongId = appointmentRepository.findByIdAndStatus(appointment2.getId(), AppointmentStatus.CONFIRMED);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(appointment1.getId());
        assertThat(found.get().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(found.get().getPatient().getId()).isEqualTo(patient.getId());
        assertThat(found.get().getDoctor().getId()).isEqualTo(doctor.getId());

        // Verificación inversa
        assertThat(notFoundWrongStatus).isNotPresent();
        assertThat(notFoundWrongId).isNotPresent();
    }

    // De aquí en adelante me falta revisar Atte - Juan
    @Test
    @DisplayName("Debe detectar solapamiento de citas para un paciente")
    void shouldExistsOverlapForPatient() {
        // Given
        var now = LocalDateTime.now();

        // Cita existente de 10:00 a 10:30
        appointmentRepository.save(buildDefaultAppointment(now, AppointmentStatus.CONFIRMED));

        // When - intenta agendar de 10:15 a 10:45 (se solapa)
        var overlaps = appointmentRepository.existsOverlapForPatient(
            patient.getId(), now.plusMinutes(15), now.plusMinutes(45)
        );

        // Intenta agendar de 10:30 a 11:00 (no se solapa, empieza exactamente cuando termina)
        var noOverlapAfter = appointmentRepository.existsOverlapForPatient(
            patient.getId(), now.plusMinutes(30), now.plusMinutes(60)
        );

        // Intenta agendar de 09:00 a 09:30 (no se solapa, termina antes)
        var noOverlapBefore = appointmentRepository.existsOverlapForPatient(
            patient.getId(), now.minusMinutes(30), now
        );

        // Cita CANCELLED no debe contar como solapamiento
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor).office(office)
            .appointmentType(appointmentType)
            .startAt(now.plusMinutes(15))
            .endAt(now.plusMinutes(45))
            .status(AppointmentStatus.CANCELLED)
            .createdAt(Instant.now())
            .build()
        );
        var cancelledDoesNotOverlap = appointmentRepository.existsOverlapForPatient(
            patient.getId(), now.plusMinutes(15), now.plusMinutes(45)
        );

        // Then
        assertThat(overlaps).isTrue();

        // Verificación inversa
        assertThat(noOverlapAfter).isFalse();
        assertThat(noOverlapBefore).isFalse();
        assertThat(cancelledDoesNotOverlap).isTrue(); // sigue siendo true por la CONFIRMED
    }

    @Test
    @DisplayName("Debe detectar solapamiento de citas para un doctor")
    void shouldExistsOverlapForDoctor() {
        // Given
        var now = LocalDateTime.now();
        appointmentRepository.save(buildDefaultAppointment(now, AppointmentStatus.CONFIRMED));

        // When
        var overlaps = appointmentRepository.existsOverlapForDoctor(
            doctor.getId(), now.plusMinutes(15), now.plusMinutes(45)
        );
        var noOverlap = appointmentRepository.existsOverlapForDoctor(
            doctor.getId(), now.plusMinutes(30), now.plusMinutes(60)
        );
        var noOverlapDifferentDoctor = appointmentRepository.existsOverlapForDoctor(
            UUID.randomUUID(), now.plusMinutes(15), now.plusMinutes(45)
        );

        // Then
        assertThat(overlaps).isTrue();

        // Verificación inversa
        assertThat(noOverlap).isFalse();
        assertThat(noOverlapDifferentDoctor).isFalse();
    }

    @Test
    @DisplayName("Debe detectar solapamiento de citas para una oficina")
    void shouldExistsOverlapForOffice() {
        // Given
        var now = LocalDateTime.now();
        appointmentRepository.save(buildDefaultAppointment(now, AppointmentStatus.CONFIRMED));

        // When
        var overlaps = appointmentRepository.existsOverlapForOffice(
            office.getId(), now.plusMinutes(15), now.plusMinutes(45)
        );
        var noOverlap = appointmentRepository.existsOverlapForOffice(
            office.getId(), now.plusMinutes(30), now.plusMinutes(60)
        );
        var noOverlapDifferentOffice = appointmentRepository.existsOverlapForOffice(
            UUID.randomUUID(), now.plusMinutes(15), now.plusMinutes(45)
        );

        // Then
        assertThat(overlaps).isTrue();

        // Verificación inversa
        assertThat(noOverlap).isFalse();
        assertThat(noOverlapDifferentOffice).isFalse();
    }

    @Test
    @DisplayName("Debe encontrar citas de un doctor en un rango de fechas")
    void shouldFindAppointmentsByDoctorBetween() {
        // Given
        var now = LocalDateTime.now();
        var from = now.minusDays(1);
        var to = now.plusDays(1);

        var appointment1 = appointmentRepository.save(buildDefaultAppointment(now, AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildDefaultAppointment(now.plusHours(1), AppointmentStatus.CONFIRMED));

        // Fuera del rango - no debe aparecer
        appointmentRepository.save(buildDefaultAppointment(now.plusDays(5), AppointmentStatus.CONFIRMED));

        // CANCELLED dentro del rango - no debe aparecer
        appointmentRepository.save(buildDefaultAppointment(now.plusHours(2), AppointmentStatus.CANCELLED));

        // When
        var found = appointmentRepository.findAppointmentsByDoctorBetween(
            doctor.getId(), from, to
        );

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Appointment::getId)
            .containsExactlyInAnyOrder(appointment1.getId(), appointment2.getId());
        assertThat(found).extracting(a -> a.getDoctor().getId())
            .containsOnly(doctor.getId());
        assertThat(found).extracting(Appointment::getStartAt)
            .allMatch(start -> !start.isBefore(from) && start.isBefore(to));

        // Verificación inversa - CANCELLED no aparece
        assertThat(found).extracting(Appointment::getStatus)
            .doesNotContain(AppointmentStatus.CANCELLED);

        // Verificación inversa - cita fuera del rango no aparece
        assertThat(found).extracting(Appointment::getStartAt)
            .allMatch(start -> !start.isAfter(to));
    }

}