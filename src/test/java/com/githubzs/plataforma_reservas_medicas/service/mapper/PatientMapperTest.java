package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

@SpringBootTest
class PatientMapperTest {

    @Autowired
    private PatientMapper mapper;

    @Test
    void toEntityShouldMapCreateRequestToPatientEntity() {
        // Given
        PatientCreateRequest request = new PatientCreateRequest("Juan Pérez", "juan@example.com", "3103456789", "D123456789", "E12345");

        // When
        Patient entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getFullName()).isEqualTo("Juan Pérez");
        assertThat(entity.getEmail()).isEqualTo("juan@example.com");
        assertThat(entity.getPhoneNumber()).isEqualTo("3103456789");
        assertThat(entity.getDocumentNumber()).isEqualTo("D123456789");
        assertThat(entity.getStudentCode()).isEqualTo("E12345");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
    }

    @Test
    void toResponseShouldMapPatientToResponse() {
        // Given
        UUID patientId = UUID.randomUUID();
        Instant now = Instant.now();
        Patient patient = Patient.builder()
            .id(patientId)
            .fullName("Juan Pérez")
            .email("juan@example.com")
            .phoneNumber("3103456789")
            .documentNumber("D123456789")
            .studentCode("E12345")
            .status(PatientStatus.ACTIVE)
            .createdAt(now)
            .build();

        // When
        PatientResponse response = mapper.toResponse(patient);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(patientId);
        assertThat(response.fullName()).isEqualTo("Juan Pérez");
        assertThat(response.email()).isEqualTo("juan@example.com");
        assertThat(response.phoneNumber()).isEqualTo("3103456789");
        assertThat(response.status()).isEqualTo(PatientStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(now);
    }

    @Test
    void patchShouldUpdateOnlyNonNullFields() {
        // Given
        UUID patientId = UUID.randomUUID();
        Instant now = Instant.now();
        Patient patient = Patient.builder()
            .id(patientId)
            .fullName("Juan Pérez")
            .email("juan@example.com")
            .phoneNumber("3103456789")
            .documentNumber("D123456789")
            .studentCode("E12345")
            .status(PatientStatus.ACTIVE)
            .createdAt(now)
            .build();

        PatientUpdateRequest request = new PatientUpdateRequest("Carlos Torres", "3215678901", null);

        // When
        mapper.patch(request, patient);

        // Then
        assertThat(patient.getFullName()).isEqualTo("Carlos Torres");
        assertThat(patient.getPhoneNumber()).isEqualTo("3215678901");
        assertThat(patient.getEmail()).isEqualTo("juan@example.com"); // Sin cambios
        assertThat(patient.getDocumentNumber()).isEqualTo("D123456789"); // Sin cambios
        assertThat(patient.getStudentCode()).isEqualTo("E12345"); // Sin cambios
    }

}
