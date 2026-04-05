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

class AppointmentRepositoryIntegrationTest extends AbstractRepositoryIT {

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
    private Specialty specialty;
    
    // Fecha y hora base para todos los test
    private LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

    // BeforeEach asegura que en cada test partimos de un estado limpio y consistente, evitando que los tests se afecten entre sí
    @BeforeEach
    void setUp() {
        specialty = specialtyRepository.save(
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
        appointmentRepository.save(
            Appointment.builder()
                .patient(patient2)
                .doctor(doctor)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(baseDateTime.plusHours(4))
                .endAt(baseDateTime.plusHours(4).plusMinutes(appointmentType.getDurationMinutes()))
                .status(AppointmentStatus.CANCELLED)
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
    @DisplayName("Appointment: No encuentra citas cuando el paciente coincide pero el estado no")
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
    @DisplayName("Appointment: No encuentra citas cuando el estado coincide pero el paciente no")
    void shouldReturnEmptyWhenPatientDoesNotMatchForFindByPatientIdAndStatus() {
        // Given
        var altId = new UUID(patient.getId().getMostSignificantBits(), patient.getId().getLeastSignificantBits() + 1);
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));
        
        // When
        var found = appointmentRepository.findByPatient_IdAndStatus(
            altId, AppointmentStatus.CONFIRMED, Pageable.ofSize(10)
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
    @DisplayName("Appointment: No encuentra citas cuando el rango de fechas no coincide")
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

        // When
        var found = appointmentRepository.findByIdAndStatus(appointment1.getId(), AppointmentStatus.CONFIRMED);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(appointment1.getId());
        assertThat(found.get().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(found.get().getPatient().getId()).isEqualTo(patient.getId());
        assertThat(found.get().getDoctor().getId()).isEqualTo(doctor.getId());
    }

    @Test
    @DisplayName("Appointment: No encuentra una cita cuando el ID coincide pero el estado no")
    void shouldReturnEmptyWhenStatusDoesNotMatchForFindByIdAndStatus() {
        // Given
        var appointment1 = appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CANCELLED));

        // When
        var found = appointmentRepository.findByIdAndStatus(appointment1.getId(), AppointmentStatus.CONFIRMED);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Appointment: No encuentra una cita cuando el estado coincide pero el ID no")
    void shouldReturnEmptyWhenIdDoesNotMatchForFindByIdAndStatus() {
        // Given
        var appointment1 = appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));
        var altId = new UUID(appointment1.getId().getMostSignificantBits(), appointment1.getId().getLeastSignificantBits() + 1);

        // When
        var found = appointmentRepository.findByIdAndStatus(altId, AppointmentStatus.CONFIRMED);

        // Then
        assertThat(found).isEmpty();
    }
        

    @Test
    @DisplayName("Appointment: Detecta solapamiento de citas para un paciente")
    void shouldExistsOverlapForPatient() {
        // Given - la hora de baseDateTime es de 10:00 a 10:30
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));

        // When - solapamiento parcial
        var overlaps = appointmentRepository.existsOverlapForPatient(
            patient.getId(), baseDateTime.plusMinutes(15), baseDateTime.plusMinutes(45)
        );

        // When - solapamiento total
        var overlapsTotal = appointmentRepository.existsOverlapForPatient(
            patient.getId(), baseDateTime, baseDateTime.plusMinutes(30)
        );

        // When - no solapamiento (empieza exactamente al terminar la cita existente)
        var noOverlapAfter = appointmentRepository.existsOverlapForPatient(
            patient.getId(), baseDateTime.plusMinutes(30), baseDateTime.plusMinutes(60)
        );

        // When - no solapamiento (termina exactamente al empezar la cita existente)
        var noOverlapBefore = appointmentRepository.existsOverlapForPatient(
            patient.getId(), baseDateTime.minusMinutes(30), baseDateTime
        );

        // Then
        assertThat(overlaps).isTrue();
        assertThat(overlapsTotal).isTrue();
        assertThat(noOverlapAfter).isFalse();
        assertThat(noOverlapBefore).isFalse();
    }

    @Test
    @DisplayName("Appointment: No detecta solapamiento de citas para un paciente cuando la cita existente está CANCELLED")
    void shouldReturnFalseWhenOverlapIsCancelledForPatient() {
        // Given - la hora de baseDateTime es de 10:00 a 10:30
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CANCELLED));

        // When - solapamiento parcial
        var overlaps = appointmentRepository.existsOverlapForPatient(
            patient.getId(), baseDateTime.plusMinutes(15), baseDateTime.plusMinutes(45)
        );

        // Then
        assertThat(overlaps).isFalse();
    }

    @Test
    @DisplayName("Appointment: Detecta solapamiento de citas para un doctor")
    void shouldExistsOverlapForDoctor() {
        // Given - la hora de baseDateTime es de 10:00 a 10:30
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));

        // When - solapamiento parcial
        var overlaps = appointmentRepository.existsOverlapForDoctor(
            doctor.getId(), baseDateTime.plusMinutes(15), baseDateTime.plusMinutes(45)
        );

        // When - solapamiento total
        var overlapsTotal = appointmentRepository.existsOverlapForDoctor(
            doctor.getId(), baseDateTime, baseDateTime.plusMinutes(30)
        );

        // When - no solapamiento (empieza exactamente al terminar la cita existente)
        var noOverlapAfter = appointmentRepository.existsOverlapForDoctor(
            doctor.getId(), baseDateTime.plusMinutes(30), baseDateTime.plusMinutes(60)
        );

        // When - no solapamiento (termina exactamente al empezar la cita existente)
        var noOverlapBefore = appointmentRepository.existsOverlapForDoctor(
            doctor.getId(), baseDateTime.minusMinutes(30), baseDateTime
        );

        // Then
        assertThat(overlaps).isTrue();
        assertThat(overlapsTotal).isTrue();
        assertThat(noOverlapAfter).isFalse();
        assertThat(noOverlapBefore).isFalse();
    }

    @Test
    @DisplayName("Appointment: No detecta solapamiento de citas para un doctor cuando la cita existente está CANCELLED")
    void shouldReturnFalseWhenOverlapIsCancelledForDoctor() {
        // Given - la hora de baseDateTime es de 10:00 a 10:30
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CANCELLED));

        // When - solapamiento parcial
        var overlaps = appointmentRepository.existsOverlapForDoctor(
            doctor.getId(), baseDateTime.plusMinutes(15), baseDateTime.plusMinutes(45)
        );

        // Then
        assertThat(overlaps).isFalse();
    }

    @Test
    @DisplayName("Appointment: Detecta solapamiento de citas para un consultorio")
    void shouldExistsOverlapForOffice() {
        // Given - la hora de baseDateTime es de 10:00 a 10:30
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));

        // When - solapamiento parcial
        var overlaps = appointmentRepository.existsOverlapForOffice(
            office.getId(), baseDateTime.plusMinutes(15), baseDateTime.plusMinutes(45)
        );

        // When - solapamiento total
        var overlapsTotal = appointmentRepository.existsOverlapForOffice(
            office.getId(), baseDateTime, baseDateTime.plusMinutes(30)
        );

        // When - no solapamiento (empieza exactamente al terminar la cita existente)
        var noOverlapAfter = appointmentRepository.existsOverlapForOffice(
            office.getId(), baseDateTime.plusMinutes(30), baseDateTime.plusMinutes(60)
        );

        // When - no solapamiento (termina exactamente al empezar la cita existente)
        var noOverlapBefore = appointmentRepository.existsOverlapForOffice(
            office.getId(), baseDateTime.minusMinutes(30), baseDateTime
        );

        // Then
        assertThat(overlaps).isTrue();
        assertThat(overlapsTotal).isTrue();
        assertThat(noOverlapAfter).isFalse();
        assertThat(noOverlapBefore).isFalse();
    }

    @Test
    @DisplayName("Appointment: No detecta solapamiento de citas para un consultorio cuando la cita existente está CANCELLED")
    void shouldReturnFalseWhenOverlapIsCancelledForOffice() {
        // Given - la hora de baseDateTime es de 10:00 a 10:30
        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CANCELLED));

        // When - solapamiento parcial
        var overlaps = appointmentRepository.existsOverlapForOffice(
            office.getId(), baseDateTime.plusMinutes(15), baseDateTime.plusMinutes(45)
        );

        // Then
        assertThat(overlaps).isFalse();
    }

    @Test
    @DisplayName("Appointment: Encuentra citas de un doctor por el rango de fechas indicado (excluyendo el límite superior)")
    void shouldFindByDoctorIdBetweenExcludeTo() {
        // Given
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        var doctor2 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr.Strange")
                .documentNumber("666666")
                .licenseNumber("LIC-002")
                .email("drstrange@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );

        var appointment1 = appointmentRepository.save(buildDefaultAppointment(baseDateTime.minusDays(1), AppointmentStatus.CONFIRMED));
        var appointment2 = appointmentRepository.save(buildDefaultAppointment(baseDateTime.plusHours(1), AppointmentStatus.CONFIRMED));

        // Citas fuera del rango - no deben aparecer
        appointmentRepository.save(buildDefaultAppointment(baseDateTime.minusDays(5), AppointmentStatus.CONFIRMED));
        appointmentRepository.save(buildDefaultAppointment(baseDateTime.plusDays(5), AppointmentStatus.CONFIRMED));
        
        // Cita de otro doctor - no debe aparecer
        appointmentRepository.save(
            Appointment.builder()
                .patient(patient)
                .doctor(doctor2)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(baseDateTime.plusHours(2))
                .endAt(baseDateTime.plusHours(2).plusMinutes(appointmentType.getDurationMinutes()))
                .status(AppointmentStatus.CONFIRMED)
                .createdAt(Instant.now())
                .build()
        );

        // When
        var found = appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(doctor.getId(), from, to);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Appointment::getId)
            .containsExactlyInAnyOrder(appointment1.getId(), appointment2.getId());
        assertThat(found).extracting(Appointment::getDoctor)
            .allMatch(d -> d.getId().equals(doctor.getId()));
        assertThat(found).extracting(Appointment::getStartAt)
            .allMatch(start -> !start.isBefore(from) && start.isBefore(to));
    }

    @Test
    @DisplayName("Appointment: Encuentra citas de un doctor por el rango de fechas indicado incluyendo el límite inferior")
    void shouldIncludeFromForFindByDoctorIdBetweenExcludeTo() {
        // Given
        var from = baseDateTime;
        var to = baseDateTime.plusHours(1);

        var appointment1 = appointmentRepository.save(buildDefaultAppointment(from, AppointmentStatus.CONFIRMED));
        
        // When
        var found = appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(doctor.getId(), from, to);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(Appointment::getId)
            .containsExactly(appointment1.getId());
        assertThat(found).extracting(Appointment::getStartAt)
            .allMatch(start -> !start.isBefore(from) && start.isBefore(to));
    }

    @Test
    @DisplayName("Appointment: No encuentra citas de un doctor por el rango de fechas indicado cuando la cita existente empieza exactamente al límite superior")
    void shouldReturnEmptyWhenStartAtIsToForFindByDoctorIdBetweenExcludeTo() {
        // Given
        var from = baseDateTime;
        var to = baseDateTime.plusHours(1);

        appointmentRepository.save(buildDefaultAppointment(to, AppointmentStatus.CONFIRMED));
        
        // When
        var found = appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(doctor.getId(), from, to);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Appointment: No encuentra citas de un doctor por el rango de fechas indicado cuando la cita existente tiene estado cancelado")
    void shouldReturnEmptyWhenStatusIsCancelledForFindByDoctorIdBetweenExcludeTo() {
        // Given
        var from = baseDateTime;
        var to = baseDateTime.plusHours(1);

        appointmentRepository.save(buildDefaultAppointment(from, AppointmentStatus.CANCELLED));
        
        // When
        var found = appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(doctor.getId(), from, to);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Appointment: No encuentra citas de un doctor cuando el rango de fechas no coincide")
    void shouldReturnEmptyWhenNoAppointmentsInRangeForFindByDoctorIdBetweenExcludeTo() {
        // Given
        var from = baseDateTime.minusDays(5);
        var to = baseDateTime.minusDays(4);

        appointmentRepository.save(buildDefaultAppointment(baseDateTime, AppointmentStatus.CONFIRMED));

        // When
        var found = appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(doctor.getId(), from, to);

        // Then
        assertThat(found).isEmpty();
    }

}