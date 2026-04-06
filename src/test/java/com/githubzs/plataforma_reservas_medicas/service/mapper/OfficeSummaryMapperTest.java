package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

@SpringBootTest
class OfficeSummaryMapperTest {

    @Autowired
    private OfficeSummaryMapper mapper;

    @Test
    void toSummaryResponseShouldMapOfficeToSummary() {
        // Given
        UUID officeId = UUID.randomUUID();
        Instant now = Instant.now();
        Office office = Office.builder()
            .id(officeId)
            .name("Consultorio medicina")
            .location("Edificio A")
            .roomNumber(101)
            .status(OfficeStatus.AVAILABLE)
            .createdAt(now)
            .build();

        // When
        OfficeSummaryResponse response = mapper.toSummaryResponse(office);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(officeId);
        assertThat(response.name()).isEqualTo("Consultorio medicina");
        assertThat(response.location()).isEqualTo("Edificio A");
        assertThat(response.roomNumber()).isEqualTo(101);
        assertThat(response.status()).isEqualTo(OfficeStatus.AVAILABLE);
    }

}
