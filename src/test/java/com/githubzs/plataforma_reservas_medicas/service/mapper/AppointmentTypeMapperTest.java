package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;

@SpringBootTest
class AppointmentTypeMapperTest {

    @Autowired
    private AppointmentTypeMapper mapper;

    @Test
    void toEntityShouldMapCreateRequestToEntity() {
        // Given
        AppointmentTypeCreateRequest request = new AppointmentTypeCreateRequest("Consulta general", null, 30);

        // When
        AppointmentType entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Consulta general");
        assertThat(entity.getDurationMinutes()).isEqualTo(30);
        assertThat(entity.getId()).isNull();
    }

    @Test
    void toResponseShouldMapEntityToResponse() {
        // Given
        UUID typeId = UUID.randomUUID();
        AppointmentType appointmentType = AppointmentType.builder()
            .id(typeId)
            .name("Consulta general")
            .durationMinutes(30)
            .build();

        // When
        AppointmentTypeResponse response = mapper.toResponse(appointmentType);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(typeId);
        assertThat(response.name()).isEqualTo("Consulta general");
        assertThat(response.durationMinutes()).isEqualTo(30);
    }

}
