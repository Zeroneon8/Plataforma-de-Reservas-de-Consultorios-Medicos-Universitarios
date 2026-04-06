package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

@SpringBootTest
class PatientSummaryMapperTest {

    @Autowired
    private PatientSummaryMapper mapper;

    @Test
    void toSummaryResponseShouldMapPatientToSummary() {
        // Given
        UUID patientId = UUID.randomUUID();
        Instant now = Instant.now();
        Patient patient = Patient.builder()
            .id(patientId)
            .fullName("Juan Pérez")
            .email("juan@example.com")
            .phoneNumber("3103456789")
            .status(PatientStatus.ACTIVE)
            .createdAt(now)
            .build();

        // When
        PatientSummaryResponse response = mapper.toSummaryResponse(patient);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(patientId);
        assertThat(response.fullName()).isEqualTo("Juan Pérez");
        assertThat(response.email()).isEqualTo("juan@example.com");
        assertThat(response.phoneNumber()).isEqualTo("3103456789");
        assertThat(response.status()).isEqualTo(PatientStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(now);
    }

}
