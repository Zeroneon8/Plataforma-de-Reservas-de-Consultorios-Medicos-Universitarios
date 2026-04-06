package com.githubzs.plataforma_reservas_medicas.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

@SpringBootTest
class OfficeMapperTest {

    @Autowired
    private OfficeMapper mapper;

    @Test
    void toEntityShouldMapCreateRequestToEntity() {
        // Given
        OfficeCreateRequest request = new OfficeCreateRequest("Consultorio medicina", "Edificio A", null, 101);

        // When
        Office entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Consultorio medicina");
        assertThat(entity.getLocation()).isEqualTo("Edificio A");
        assertThat(entity.getRoomNumber()).isEqualTo(101);
        assertThat(entity.getId()).isNull();
        assertThat(entity.getStatus()).isNull();
    }

    @Test
    void toResponseShouldMapEntityToResponse() {
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
        OfficeResponse response = mapper.toResponse(office);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(officeId);
        assertThat(response.name()).isEqualTo("Consultorio medicina");
        assertThat(response.location()).isEqualTo("Edificio A");
        assertThat(response.roomNumber()).isEqualTo(101);
        assertThat(response.status()).isEqualTo(OfficeStatus.AVAILABLE);
    }

    @Test
    void patchShouldUpdateOnlyNonNullFields() {
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

        OfficeUpdateRequest request = new OfficeUpdateRequest("Consultorio renovado", "Edificio B", null, null);

        // When
        mapper.patch(request, office);

        // Then
        assertThat(office.getName()).isEqualTo("Consultorio renovado");
        assertThat(office.getLocation()).isEqualTo("Edificio B");
        assertThat(office.getRoomNumber()).isEqualTo(101); // Sin cambios
    }

}
