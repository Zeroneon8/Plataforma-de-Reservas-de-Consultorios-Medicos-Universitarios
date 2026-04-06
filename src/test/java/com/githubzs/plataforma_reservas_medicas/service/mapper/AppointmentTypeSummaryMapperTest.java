package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;

@SpringBootTest
class AppointmentTypeSummaryMapperTest {

    @Autowired
    private AppointmentTypeSummaryMapper mapper;

    @Test
    void toSummaryResponseShouldMapAppointmentTypeToSummary() {
        // Given
        UUID typeId = UUID.randomUUID();
        AppointmentType appointmentType = AppointmentType.builder()
            .id(typeId)
            .name("Consulta general")
            .durationMinutes(30)
            .build();

        // When
        AppointmentTypeSummaryResponse response = mapper.toSummaryResponse(appointmentType);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(typeId);
        assertThat(response.name()).isEqualTo("Consulta general");
        assertThat(response.durationMinutes()).isEqualTo(30);
    }

}
