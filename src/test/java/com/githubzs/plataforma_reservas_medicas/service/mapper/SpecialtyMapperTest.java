package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;

@SpringBootTest
class SpecialtyMapperTest {

    @Autowired
    private SpecialtyMapper mapper;

    @Test
    void toEntityShouldMapCreateRequestToEntity() {
        // Given
        SpecialtyCreateRequest request = new SpecialtyCreateRequest("Medicina General", null);

        // When
        Specialty entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Medicina General");
        assertThat(entity.getId()).isNull();
    }

    @Test
    void toResponseShouldMapEntityToResponse() {
        // Given
        UUID specialtyId = UUID.randomUUID();
        Specialty specialty = Specialty.builder()
            .id(specialtyId)
            .name("Medicina General")
            .build();

        // When
        SpecialtyResponse response = mapper.toResponse(specialty);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(specialtyId);
        assertThat(response.name()).isEqualTo("Medicina General");
    }

}
