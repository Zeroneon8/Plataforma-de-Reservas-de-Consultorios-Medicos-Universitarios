package com.githubzs.plataforma_reservas_medicas.Repositories;


import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;



class OfficeRepositoryIntegrationTest extends AbstractRepositoryIT{

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

    // Dato base común para todos los tests
    private Office office;

    @BeforeEach
    void setUp() {
        office = officeRepository.save(
            Office.builder()
                .name("Consultorio de medicina general")
                .location("Edificio de bienestar universitario")
                .roomNumber(101)
                .createdAt(Instant.now())
                .status(OfficeStatus.AVAILABLE)
                .build()
        );
    }



    @Test
    @DisplayName("Office: Encuentra consultorios por estado")
    void shouldFindByStatus() {
        // Given
        officeRepository.save(
            Office.builder()
            .name("Consultorio de odontología")
            .location("Edificio sur")
            .roomNumber(305)
            .createdAt(Instant.now())
            .status(OfficeStatus.AVAILABLE)
            .build()
        );

        officeRepository.save(
            Office.builder()
            .name("Consultorio de pediatría")
            .location("Edificio norte")
            .roomNumber(202)
            .createdAt(Instant.now())
            .status(OfficeStatus.UNAVAILABLE)
            .build()
        );

        officeRepository.save(
            Office.builder()
            .name("Consultorio de ginecología")
            .location("Edificio sur")
            .roomNumber(303)
            .createdAt(Instant.now())
            .status(OfficeStatus.MAINTENANCE)
            .build()
        );

        // When
        var found = officeRepository.findByStatus(OfficeStatus.AVAILABLE, Pageable.ofSize(10));

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Office::getStatus).containsOnly(OfficeStatus.AVAILABLE);
        assertThat(found).extracting(Office::getName)
            .containsExactlyInAnyOrder("Consultorio de medicina general", "Consultorio de odontología");
        assertThat(found).extracting(Office::getRoomNumber)
            .containsExactlyInAnyOrder(101, 305);
        assertThat(found).extracting(Office::getLocation)
            .containsExactlyInAnyOrder("Edificio de bienestar universitario", "Edificio sur");
    }

    @Test
    @DisplayName("Office: No encuentra consultorios si el estado no coincide")
    void shouldReturnEmptyWhenStatusDoesNotMatchForFindByStatus() {
        // Given
        officeRepository.save(
            Office.builder()
            .name("Consultorio de pediatría")
            .location("Edificio norte")
            .roomNumber(202)
            .createdAt(Instant.now())
            .status(OfficeStatus.UNAVAILABLE)
            .build()
        );

        // When
        var found = officeRepository.findByStatus(OfficeStatus.MAINTENANCE, Pageable.ofSize(10));

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Office: Calcula la ocupación de las oficinas entre dos fechas")
    void shouldCalculateOfficeOccupancyBetween() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);
        
        var office2 = officeRepository.save(
            Office.builder()
                .name("Consultorio de pediatría")
                .location("Edificio norte")
                .roomNumber(202)
                .createdAt(Instant.now())
                .status(OfficeStatus.AVAILABLE)
                .build()
        );

        var office3 = officeRepository.save(
            Office.builder()
                .name("Consultorio de ginecología")
                .location("Edificio sur")
                .roomNumber(303)
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

        var specialty = specialtyRepository.save(
            Specialty.builder()
                .name("Medicina General")
                .build()
        );

        var doctor = doctorRepository.save(
            Doctor.builder()
                .fullName("Dr. House")
                .documentNumber("111111")
                .licenseNumber("11111111")
                .email("drHouse123@doctor.com")
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
                .email("JohnDoe123@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );

        // Citas para office
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime)
            .endAt(baseDateTime.plusMinutes(30))
            .status(AppointmentStatus.SCHEDULED)
            .createdAt(Instant.now())
            .build()
        );

        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusHours(3))
            .endAt(baseDateTime.plusHours(3).plusMinutes(30))
            .status(AppointmentStatus.NO_SHOW)
            .createdAt(Instant.now())
            .build()
        );

        // Cita para office2
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office2)
            .appointmentType(appointmentType)
            .startAt(baseDateTime.plusHours(6))
            .endAt(baseDateTime.plusHours(6).plusMinutes(30))
            .status(AppointmentStatus.CONFIRMED)
            .createdAt(Instant.now())
            .build()
        );

        // when
        var result = officeRepository.calculateOfficeOccupancyBetween(from, to);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("officeId")
            .containsExactly(office.getId(), office2.getId(), office3.getId());
        assertThat(result).extracting("officeName")
            .containsExactly(office.getName(), office2.getName(), office3.getName());
        assertThat(result).extracting("officeLocation")
            .containsExactly(office.getLocation(), office2.getLocation(), office3.getLocation());
        assertThat(result).extracting("roomNumber")
            .containsExactly(office.getRoomNumber(), office2.getRoomNumber(), office3.getRoomNumber());
        assertThat(result).extracting("appointmentCount")
            .containsExactly(2L, 1L, 0L);
        assertThat(result).extracting("minutesOccupied")
            .containsExactly(60L, 30L, 0L);
        assertThat(result).extracting("noShowCount")
            .containsExactly(1L, 0L, 0L);
    }  
    

    @Test
    @DisplayName("Office: No incluye citas canceladas al calcular la ocupación de las oficinas entre dos fechas")
    void shouldNotIncludeCancelledAppointmentsWhenCalculatingOfficeOccupancyBetween() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        var appointmentType = appointmentTypeRepository.save(
            AppointmentType.builder()
                .name("Consulta general")
                .durationMinutes(30)
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
                .licenseNumber("11111111")
                .email("drHouse123@doctor.com")
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
                .email("JohnDoe123@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );

        // Citas canceladas para office
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
            .startAt(baseDateTime.plusHours(3))
            .endAt(baseDateTime.plusHours(3).plusMinutes(30))
            .status(AppointmentStatus.CANCELLED)
            .createdAt(Instant.now())
            .build()
        );

        // when
        var result = officeRepository.calculateOfficeOccupancyBetween(from, to);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("officeId")
            .containsExactly(office.getId());
        assertThat(result).extracting("officeName")
            .containsExactly(office.getName());
        assertThat(result).extracting("officeLocation")
            .containsExactly(office.getLocation());
        assertThat(result).extracting("roomNumber")
            .containsExactly(office.getRoomNumber());
        assertThat(result).extracting("appointmentCount")
            .containsExactly(0L);
        assertThat(result).extracting("minutesOccupied")
            .containsExactly(0L);
        assertThat(result).extracting("noShowCount")
            .containsExactly(0L);
    }

    @Test
    @DisplayName("Office: No incluye citas extremas en el borde del rango al calcular la ocupación de las oficinas entre dos fechas")
    void shouldNotIncludeExtremeBorderAppointmentsWhenCalculatingOfficeOccupancyBetween() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime;
        var to = baseDateTime.plusHours(1);

        var appointmentType = appointmentTypeRepository.save(
            AppointmentType.builder()
                .name("Consulta general")
                .durationMinutes(30)
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
                .licenseNumber("11111111")
                .email("drHouse123@doctor.com")
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
                .email("JohnDoe123@gmail.com")
                .createdAt(Instant.now())
                .status(PatientStatus.ACTIVE)
                .build()
        );

        // Cita que empieza exactamente en 'To'
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(to)
            .endAt(to.plusMinutes(30))
            .status(AppointmentStatus.SCHEDULED)
            .createdAt(Instant.now())
            .build()
        );
        
        // Cita que termina exactamente en 'From'
        appointmentRepository.save(Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(from.minusMinutes(30))
            .endAt(from)
            .status(AppointmentStatus.SCHEDULED)
            .createdAt(Instant.now())
            .build()
        );

        // when
        var result = officeRepository.calculateOfficeOccupancyBetween(from, to);
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("officeId")
            .containsExactly(office.getId());
        assertThat(result).extracting("officeName")
            .containsExactly(office.getName());
        assertThat(result).extracting("officeLocation")
            .containsExactly(office.getLocation());
        assertThat(result).extracting("roomNumber")
            .containsExactly(office.getRoomNumber());
        assertThat(result).extracting("appointmentCount")
            .containsExactly(0L);
        assertThat(result).extracting("minutesOccupied")
            .containsExactly(0L);
        assertThat(result).extracting("noShowCount")
            .containsExactly(0L);
    }

    @Test
    @DisplayName("Office: Calculo de ocupación sin oficinas registradas devuelve lista vacía")
    void shouldReturnEmptyWhenNoOfficesForCalculateOfficeOccupancyBetween() {
        // Given
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var from = baseDateTime.minusDays(1);
        var to = baseDateTime.plusDays(1);

        officeRepository.deleteAll();

        // when
        var result = officeRepository.calculateOfficeOccupancyBetween(from, to);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Office: Detecta si existe un consultorio por su nombre ignorando mayúsculas y minúsculas")
    void shouldExistByNameIgnoreCase() {
        // Given - ya creado en el setUp() con nombre "Consultorio de medicina general"

        // When
        var existLowerCase = officeRepository.existsByNameIgnoreCase("consultorio de medicina general");
        var existUpperCase = officeRepository.existsByNameIgnoreCase("CONSULTORIO DE MEDICINA GENERAL");
        var existMixedCase = officeRepository.existsByNameIgnoreCase("Consultorio De Medicina General");

        // Then
        assertThat(existLowerCase).isTrue();
        assertThat(existUpperCase).isTrue();
        assertThat(existMixedCase).isTrue();
    }

    @Test
    @DisplayName("Office: No detecta la existencia de un consultorio por nombre cuando no coincide")
    void shouldReturnFalseWhenNameDoesNotExistForExistsByNameIgnoreCase() {
        // Given - ya creado en el setUp()

        // When
        var exist = officeRepository.existsByNameIgnoreCase("Consultorio inexistente");

        // Then
        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Office: Detecta si existe un consultorio por su número de sala")
    void shouldExistByRoomNumber() {
        // Given - ya creado en el setUp() con roomNumber = 101

        // When
        var exist = officeRepository.existsByRoomNumber(101);

        // Then
        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("Office: No detecta la existencia de un consultorio por número de sala cuando no coincide")
    void shouldReturnFalseWhenRoomNumberDoesNotExistForExistsByRoomNumber() {
        // Given - ya creado en el setUp()

        // When
        var exist = officeRepository.existsByRoomNumber(999);

        // Then
        assertThat(exist).isFalse();
    }

}