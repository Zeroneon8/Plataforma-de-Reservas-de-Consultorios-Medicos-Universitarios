package com.githubzs.plataforma_reservas_medicas.Repositories;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

public class DoctorRepositoryTest extends AbstractRepositoryIT {

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
    
    
    @Test
    @DisplayName("Debe encontrar un doctor por número de documento")
    void shouldFindByDocumentNumber() {
        // Given
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

        // When
        var found = doctorRepository.findByDocumentNumber("123456");
        var notFound = doctorRepository.findByDocumentNumber("999999");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(doctor.getId());
        assertThat(found.get().getFullName()).isEqualTo("Dr. House");
        assertThat(found.get().getDocumentNumber()).isEqualTo("123456");
        assertThat(found.get().getEmail()).isEqualTo("drhouse@doctor.com");
        assertThat(found.get().getStatus()).isEqualTo(DoctorStatus.ACTIVE);
        assertThat(found.get().getSpecialty().getId()).isEqualTo(specialty.getId());

        // Verificación inversa
        assertThat(notFound).isNotPresent();
    }

    @Test
    @DisplayName("Debe encontrar doctores por status y especialidad")
    void shouldFindByStatusAndSpecialty_Id() {
        // Given
        var specialty1 = specialtyRepository.save(
            Specialty.builder()
                .name("Medicina General")
                .build()
        );

        var specialty2 = specialtyRepository.save(
            Specialty.builder()
                .name("Psicología")
                .build()
        );

        var doctor1 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. House")
                .documentNumber("111111")
                .licenseNumber("LIC-001")
                .email("drhouse@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty1)
                .createdAt(Instant.now())
                .build()
        );

        var doctor2 = doctorRepository.save(
            Doctor.builder()
                .fullName("Dra. Grey")
                .documentNumber("222222")
                .licenseNumber("LIC-002")
                .email("drgrey@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty1)
                .createdAt(Instant.now())
                .build()
        );

        // Doctor INACTIVE en specialty1 - no debe aparecer
        doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. Wilson")
                .documentNumber("333333")
                .licenseNumber("LIC-003")
                .email("drwilson@doctor.com")
                .status(DoctorStatus.INACTIVE)
                .specialty(specialty1)
                .createdAt(Instant.now())
                .build()
        );

        // Doctor ACTIVE en specialty2 - no debe aparecer
        doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. Yang")
                .documentNumber("444444")
                .licenseNumber("LIC-004")
                .email("dryang@doctor.com")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty2)
                .createdAt(Instant.now())
                .build()
        );

        // When
        var found = doctorRepository.findByStatusAndSpecialty_Id(
            DoctorStatus.ACTIVE, specialty1.getId(), Pageable.ofSize(10)
        );

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Doctor::getId)
            .containsExactlyInAnyOrder(doctor1.getId(), doctor2.getId());
        assertThat(found).extracting(Doctor::getStatus)
            .containsOnly(DoctorStatus.ACTIVE);
        assertThat(found).extracting(Doctor::getSpecialty)
            .extracting(Specialty::getId)
            .containsOnly(specialty1.getId());

        // Verificación inversa - INACTIVE no aparece
        assertThat(found).extracting(Doctor::getStatus)
            .doesNotContain(DoctorStatus.INACTIVE);

        // Verificación inversa - specialty2 no aparece
        assertThat(found).extracting(Doctor::getSpecialty)
            .extracting(Specialty::getId)
            .doesNotContain(specialty2.getId());
    }

    @Test
    @DisplayName("Debe verificar si existe un doctor por ID y status")
    void shouldExistsByIdAndStatus() {
        // Given
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

        // When
        var existsWithCorrectIdAndStatus = doctorRepository.existsByIdAndStatus(
            doctor.getId(), DoctorStatus.ACTIVE
        );
        var existsWithWrongId = doctorRepository.existsByIdAndStatus(
            UUID.randomUUID(), DoctorStatus.ACTIVE
        );
        var existsWithWrongStatus = doctorRepository.existsByIdAndStatus(
            doctor.getId(), DoctorStatus.INACTIVE
        );

        // Then
        assertThat(existsWithCorrectIdAndStatus).isTrue();

        // Verificación inversa
        assertThat(existsWithWrongId).isFalse();
        assertThat(existsWithWrongStatus).isFalse();
    }

    @Test
    @DisplayName("Debe rankear doctores por citas completadas en orden descendente")
    void shouldRankDoctorsByCompletedAppointments() {
        // Given
        var now = LocalDateTime.now();

        var specialty = specialtyRepository.save(
            Specialty.builder()
                .name("Medicina General")
                .build()
        );

        var patient = patientRepository.save(
            Patient.builder()
                .fullName("John Doe")
                .documentNumber("123456")
                .phoneNumber("324-123-4567")
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

        // Doctor1 - tendrá 2 citas COMPLETED → debe quedar primero
        var doctor1 = doctorRepository.save(
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

        // Doctor2 - tendrá 1 cita COMPLETED → debe quedar segundo
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

        // Doctor3 - sin citas COMPLETED → debe quedar último con 0
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

        // 2 citas COMPLETED para doctor1
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor1).office(office)
            .appointmentType(appointmentType)
            .startAt(now).endAt(now.plusMinutes(30))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now()).build()
        );
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor1).office(office)
            .appointmentType(appointmentType)
            .startAt(now.plusDays(1)).endAt(now.plusDays(1).plusMinutes(30))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now()).build()
        );

        // 1 cita COMPLETED para doctor2
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor2).office(office)
            .appointmentType(appointmentType)
            .startAt(now).endAt(now.plusMinutes(30))
            .status(AppointmentStatus.COMPLETED)
            .createdAt(Instant.now()).build()
        );

        // Cita CANCELLED para doctor2 - no debe contar en el ranking
        appointmentRepository.save(Appointment.builder()
            .patient(patient).doctor(doctor2).office(office)
            .appointmentType(appointmentType)
            .startAt(now).endAt(now.plusMinutes(30))
            .status(AppointmentStatus.CANCELLED)
            .createdAt(Instant.now()).build()
        );

        // When
        var result = doctorRepository.rankDoctorsByCompletedAppointments();

        // Then
        assertThat(result).hasSize(3);

        // Verifica orden descendente
        var ranking1 = result.get(0);
        assertThat(ranking1.getDoctorId()).isEqualTo(doctor1.getId());
        assertThat(ranking1.getDoctorName()).isEqualTo("Dr. House");
        assertThat(ranking1.getCompletedAppointments()).isEqualTo(2);

        var ranking2 = result.get(1);
        assertThat(ranking2.getDoctorId()).isEqualTo(doctor2.getId());
        assertThat(ranking2.getDoctorName()).isEqualTo("Dra. Grey");
        assertThat(ranking2.getCompletedAppointments()).isEqualTo(1);

        // Doctor3 aparece con 0 por el LEFT JOIN
        var ranking3 = result.get(2);
        assertThat(ranking3.getDoctorId()).isEqualTo(doctor3.getId());
        assertThat(ranking3.getCompletedAppointments()).isEqualTo(0);

        // Verificación inversa - orden descendente correcto
        assertThat(result).extracting(DoctorRankingStatsDto::getCompletedAppointments)
            .isSortedAccordingTo(Comparator.reverseOrder());

        // Verificación inversa - cita CANCELLED no afectó el conteo de doctor2
        assertThat(ranking2.getCompletedAppointments()).isEqualTo(1);
    }
}
