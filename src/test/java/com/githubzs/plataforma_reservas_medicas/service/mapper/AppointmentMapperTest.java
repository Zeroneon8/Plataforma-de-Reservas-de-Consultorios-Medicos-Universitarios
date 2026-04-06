package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentResponse;
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

@SpringBootTest
class AppointmentMapperTest {

    @Autowired
    private AppointmentMapper mapper;

    @Test
    void toEntityShouldMapCreateRequestToEntity() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();
        UUID appointmentTypeId = UUID.randomUUID();
        LocalDateTime startAt = LocalDateTime.now().plusDays(3);

        AppointmentCreateRequest request = new AppointmentCreateRequest(patientId, doctorId, officeId, appointmentTypeId, startAt);

        // When
        Appointment entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getStartAt()).isEqualTo(startAt);
        assertThat(entity.getCreatedAt()).isNull();
    }

    @Test
    void toResponseShouldMapEntityToResponse() {
        // Given
        UUID appointmentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();
        UUID appointmentTypeId = UUID.randomUUID();
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusMinutes(30);
        Instant now = Instant.now();

        Patient patient = Patient.builder().id(patientId).fullName("Juan Pérez").status(PatientStatus.ACTIVE).build();
        Specialty specialty = Specialty.builder().id(UUID.randomUUID()).name("Medicina General").build();
        Doctor doctor = Doctor.builder().id(doctorId).fullName("Dr. House").status(DoctorStatus.ACTIVE).specialty(specialty).build();
        Office office = Office.builder().id(officeId).name("Consultorio 1").status(OfficeStatus.AVAILABLE).build();
        AppointmentType appointmentType = AppointmentType.builder().id(appointmentTypeId).name("Consulta general").durationMinutes(30).build();

        Appointment appointment = Appointment.builder()
            .id(appointmentId)
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(appointmentType)
            .startAt(startAt)
            .endAt(endAt)
            .status(AppointmentStatus.SCHEDULED)
            .createdAt(now)
            .build();

        // When
        AppointmentResponse response = mapper.toResponse(appointment);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(appointmentId);
        assertThat(response.startAt()).isEqualTo(startAt);
        assertThat(response.endAt()).isEqualTo(endAt);
        assertThat(response.status()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

}
