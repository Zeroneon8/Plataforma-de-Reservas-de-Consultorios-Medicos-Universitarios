package com.githubzs.plataforma_reservas_medicas.Repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Comparator;

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

public class AppointmentRepositoryTest extends AbstractRepositoryIT {

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
                .phoneNumber("324-123-4567")
                .email("johndoe@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
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

    // Helper para no repetir el builder en cada test
    private Appointment buildAppointment(LocalDateTime start, AppointmentStatus status) {
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
    @DisplayName("Debe encontrar citas por ID de paciente y status")
    void shouldFindByPatient_IdAndStatus() {
        // Given
        var now = LocalDateTime.now();
        var appointment1 = appointmentRepository.save(buildAppointment(now, AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildAppointment(now.plusHours(1), AppointmentStatus.CONFIRMED));
        appointmentRepository.save(buildAppointment(now.plusHours(2), AppointmentStatus.CANCELLED));

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

        // Verificación inversa - CANCELLED no aparece
        assertThat(found).extracting(Appointment::getStatus)
            .doesNotContain(AppointmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Debe encontrar citas dentro de un rango de fechas")
    void shouldFindByStartAtBetween() {
        // Given
        var now = LocalDateTime.now();
        var from = now.minusDays(1);
        var to = now.plusDays(1);

        var appointment1 = appointmentRepository.save(buildAppointment(now, AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildAppointment(now.plusHours(1), AppointmentStatus.CONFIRMED));

        // Fuera del rango - no debe aparecer
        appointmentRepository.save(buildAppointment(now.plusDays(5), AppointmentStatus.CONFIRMED));

        // When
        var found = appointmentRepository.findByStartAtBetween(from, to, Pageable.ofSize(10));

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Appointment::getId)
            .containsExactlyInAnyOrder(appointment1.getId(), appointment2.getId());
        assertThat(found).extracting(Appointment::getStartAt)
            .allMatch(start -> start.isAfter(from) && start.isBefore(to));

        // Verificación inversa - cita fuera del rango no aparece
        assertThat(found).extracting(Appointment::getStartAt)
            .allMatch(start -> !start.isAfter(to));
    }

    @Test
    @DisplayName("Debe encontrar una cita por ID y status")
    void shouldFindByIdAndStatus() {
        // Given
        var now = LocalDateTime.now();
        var appointment = appointmentRepository.save(buildAppointment(now, AppointmentStatus.CONFIRMED));

        // When
        var found = appointmentRepository.findByIdAndStatus(appointment.getId(), AppointmentStatus.CONFIRMED);
        var notFoundWrongStatus = appointmentRepository.findByIdAndStatus(appointment.getId(), AppointmentStatus.CANCELLED);
        var notFoundWrongId = appointmentRepository.findByIdAndStatus(UUID.randomUUID(), AppointmentStatus.CONFIRMED);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(appointment.getId());
        assertThat(found.get().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(found.get().getPatient().getId()).isEqualTo(patient.getId());
        assertThat(found.get().getDoctor().getId()).isEqualTo(doctor.getId());

        // Verificación inversa
        assertThat(notFoundWrongStatus).isNotPresent();
        assertThat(notFoundWrongId).isNotPresent();
    }

    @Test
    @DisplayName("Debe detectar solapamiento de citas para un paciente")
    void shouldExistsOverlapForPatient() {
        // Given
        var now = LocalDateTime.now();

        // Cita existente de 10:00 a 10:30
        appointmentRepository.save(buildAppointment(now, AppointmentStatus.CONFIRMED));

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
        appointmentRepository.save(buildAppointment(now, AppointmentStatus.CONFIRMED));

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
        appointmentRepository.save(buildAppointment(now, AppointmentStatus.CONFIRMED));

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

        var appointment1 = appointmentRepository.save(buildAppointment(now, AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildAppointment(now.plusHours(1), AppointmentStatus.CONFIRMED));

        // Fuera del rango - no debe aparecer
        appointmentRepository.save(buildAppointment(now.plusDays(5), AppointmentStatus.CONFIRMED));

        // CANCELLED dentro del rango - no debe aparecer
        appointmentRepository.save(buildAppointment(now.plusHours(2), AppointmentStatus.CANCELLED));

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