package com.githubzs.plataforma_reservas_medicas.Repositories;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;



public class OfficeRepositoryTest extends AbstractRepositoryIT{

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

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Test
    @DisplayName("Debe existir si se crea y busca por id y status")
    void shouldExistByIdAndStatus() {
        var office = Office.builder().name("Consultorio de medicina general").location("Edificio de bienestar universitario").roomNumber(101).createdAt(Instant.now()).status(OfficeStatus.AVAILABLE).build();
        var saved = officeRepository.save(office);

        var existsWithCorrectIdAndStatus = officeRepository.existsByIdAndStatus(saved.getId(), OfficeStatus.AVAILABLE);
        var existsWithWrongId = officeRepository.existsByIdAndStatus(UUID.randomUUID(), OfficeStatus.AVAILABLE);
        var existsWithWrongStatus = officeRepository.existsByIdAndStatus(saved.getId(), OfficeStatus.UNAVAILABLE);

        assertThat(existsWithCorrectIdAndStatus).isTrue();
        assertThat(existsWithWrongId).isFalse();
        assertThat(existsWithWrongStatus).isFalse();
    }

    @Test
    @DisplayName("Debe encontrar oficinas por status")
    void shouldFindByStatus() {
        var office1 = Office.builder().name("Consultorio de medicina general")
        .location("Edificio de bienestar universitario")
        .roomNumber(101)
        .createdAt(Instant.now())
        .status(OfficeStatus.AVAILABLE)
        .build();

        var office2 = Office.builder()
            .name("Consultorio de pediatría")
            .location("Edificio norte")
            .roomNumber(202)
            .createdAt(Instant.now())
            .status(OfficeStatus.UNAVAILABLE)
            .build();

        var saved1 = officeRepository.save(office1);

        
        var found = officeRepository.findByStatus(OfficeStatus.AVAILABLE, Pageable.ofSize(10));

        
        assertThat(found).hasSize(1);
        assertThat(found).extracting(Office::getId).containsExactly(saved1.getId());
        assertThat(found).extracting(Office::getName).containsExactly("Consultorio de medicina general");
        assertThat(found).extracting(Office::getStatus).containsOnly(OfficeStatus.AVAILABLE);
        assertThat(found).extracting(Office::getRoomNumber).containsExactly(101);
    }


    @Test
    @DisplayName("Debe calcular la ocupación de consultorios entre dos fechas")
    void shouldCalculateOfficeOccupancyBetween() {
        // Given - fechas de referencia
        var now = LocalDateTime.now();
        var from = now.minusDays(1);
        var to = now.plusDays(1);

        // Datos base necesarios
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

        var doctor = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. House")
                .documentNumber("111111")
                .email("drHouse123@doctor.com")
                .licenseNumber("11111111")
                .status(DoctorStatus.ACTIVE)
                .specialty(specialty)
                .createdAt(Instant.now())
                .build()
        );

        // Office 1 - tendrá 2 citas válidas y 1 NO_SHOW
        var office1 = officeRepository.save(
            Office.builder()
                .name("Consultorio 1")
                .location("Edificio A")
                .roomNumber(101)
                .createdAt(Instant.now())
                .status(OfficeStatus.AVAILABLE)
                .build()
        );

        // Office 2 - tendrá 1 cita válida
        var office2 = officeRepository.save(
            Office.builder()
                .name("Consultorio 2")
                .location("Edificio B")
                .roomNumber(202)
                .createdAt(Instant.now())
                .status(OfficeStatus.AVAILABLE)
                .build()
        );

        // Cita normal dentro del rango - office1
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office1)
            .appointmentType(appointmentType)
            .startAt(now)
            .endAt(now.plusMinutes(30))
            .status(AppointmentStatus.CONFIRMED)
            .createdAt(Instant.now())
            .build()
        );

        // Cita NO_SHOW dentro del rango - office1
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

        // Cita CANCELLED dentro del rango - office1 (debe excluirse del conteo)
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

        // Cita FUERA del rango - office1 (no debe contarse)
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

        // Cita normal dentro del rango - office2
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office2)
            .appointmentType(appointmentType)
            .startAt(now)
            .endAt(now.plusMinutes(30))
            .status(AppointmentStatus.CONFIRMED)
            .createdAt(Instant.now())
            .build()
        );

        // When
        var result = officeRepository.calculateOfficeOccupancyBetween(from, to);

        // Then - verifica el orden (office1 primero por tener más citas)
        assertThat(result).hasSize(2);

        // Office1: 2 citas válidas (CONFIRMED + NO_SHOW), 60 minutos, 1 no_show
        var occupancy1 = result.get(0);
        assertThat(occupancy1.getOfficeId()).isEqualTo(office1.getId());
        assertThat(occupancy1.getAppointmentCount()).isEqualTo(2);
        assertThat(occupancy1.getMinutesOccupied()).isEqualTo(60);
        assertThat(occupancy1.getNoShowCount()).isEqualTo(1);

        // Office2: 1 cita válida, 30 minutos, 0 no_show
        var occupancy2 = result.get(1);
        assertThat(occupancy2.getOfficeId()).isEqualTo(office2.getId());
        assertThat(occupancy2.getAppointmentCount()).isEqualTo(1);
        assertThat(occupancy2.getMinutesOccupied()).isEqualTo(30);
        assertThat(occupancy2.getNoShowCount()).isEqualTo(0);
    }



    
}
