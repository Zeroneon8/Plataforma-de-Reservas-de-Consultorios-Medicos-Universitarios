package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;

@SpringBootTest
class DoctorMapperTest {

    @Autowired
    private DoctorMapper mapper;

    @Test
    void toEntityShouldMapCreateRequestToEntity() {
        // Given
        UUID specialtyId = UUID.randomUUID();
        DoctorCreateRequest request = new DoctorCreateRequest("Dr. House", "house@example.com", "1111111", "LIC123", specialtyId);

        // When
        Doctor entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getFullName()).isEqualTo("Dr. House");
        assertThat(entity.getEmail()).isEqualTo("house@example.com");
        assertThat(entity.getDocumentNumber()).isEqualTo("1111111");
        assertThat(entity.getLicenseNumber()).isEqualTo("LIC123");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getSpecialty()).isNull();
    }

    @Test
    void toResponseShouldMapEntityToResponse() {
        // Given
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        Instant now = Instant.now();
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor doctor = Doctor.builder()
            .id(doctorId)
            .fullName("Dr. House")
            .email("house@example.com")
            .documentNumber("1111111")
            .licenseNumber("LIC123")
            .specialty(specialty)
            .status(DoctorStatus.ACTIVE)
            .createdAt(now)
            .build();

        // When
        DoctorResponse response = mapper.toResponse(doctor);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(doctorId);
        assertThat(response.fullName()).isEqualTo("Dr. House");
        assertThat(response.email()).isEqualTo("house@example.com");
        assertThat(response.status()).isEqualTo(DoctorStatus.ACTIVE);
    }

    @Test
    void patchShouldUpdateOnlyNonNullFields() {
        // Given
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        Instant now = Instant.now();
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor doctor = Doctor.builder()
            .id(doctorId)
            .fullName("Dr. House")
            .email("house@example.com")
            .documentNumber("1111111")
            .licenseNumber("LIC123")
            .specialty(specialty)
            .status(DoctorStatus.ACTIVE)
            .createdAt(now)
            .build();

        DoctorUpdateRequest request = new DoctorUpdateRequest("Dr. Wilson", null, specialtyId);

        // When
        mapper.patch(request, doctor);

        // Then
        assertThat(doctor.getFullName()).isEqualTo("Dr. Wilson");
        assertThat(doctor.getEmail()).isEqualTo("house@example.com"); // Sin cambios
        assertThat(doctor.getDocumentNumber()).isEqualTo("1111111"); // Sin cambios
        assertThat(doctor.getLicenseNumber()).isEqualTo("LIC123"); // Sin cambios
    }

}
