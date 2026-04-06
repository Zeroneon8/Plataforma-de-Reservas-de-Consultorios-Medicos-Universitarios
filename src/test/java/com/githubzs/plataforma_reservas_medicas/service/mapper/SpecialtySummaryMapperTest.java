package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtySummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;

@SpringBootTest
class SpecialtySummaryMapperTest {

    @Autowired
    private SpecialtySummaryMapper mapper;

    @Test
    void toSummaryResponseShouldMapSpecialtyToSummary() {
        // Given
        UUID specialtyId = UUID.randomUUID();
        Specialty specialty = Specialty.builder()
            .id(specialtyId)
            .name("Medicina General")
            .build();

        // When
        SpecialtySummaryResponse response = mapper.toSummaryResponse(specialty);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(specialtyId);
        assertThat(response.name()).isEqualTo("Medicina General");
    }

}
