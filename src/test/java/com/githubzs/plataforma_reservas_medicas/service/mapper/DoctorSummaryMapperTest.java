package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;

@SpringBootTest
class DoctorSummaryMapperTest {

    @Autowired
    private DoctorSummaryMapper mapper;

    @Test
    void toSummaryResponseShouldMapDoctorToSummary() {
        // Given
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        Instant now = Instant.now();

        Specialty specialty = Specialty.builder()
            .id(specialtyId)
            .name("Medicina General")
            .build();

        Doctor doctor = Doctor.builder()
            .id(doctorId)
            .fullName("Dr. House")
            .email("house@example.com")
            .documentNumber("1111111")
            .specialty(specialty)
            .status(DoctorStatus.ACTIVE)
            .createdAt(now)
            .build();

        // When
        DoctorSummaryResponse response = mapper.toSummaryResponse(doctor);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(doctorId);
        assertThat(response.fullName()).isEqualTo("Dr. House");
        assertThat(response.email()).isEqualTo("house@example.com");
        assertThat(response.status()).isEqualTo(DoctorStatus.ACTIVE);
    }

}
