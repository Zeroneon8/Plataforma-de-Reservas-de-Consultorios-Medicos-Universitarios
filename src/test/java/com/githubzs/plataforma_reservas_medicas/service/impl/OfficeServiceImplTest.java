package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.mapper.OfficeMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.OfficeSummaryMapper;

@ExtendWith(MockitoExtension.class)
class OfficeServiceImplTest {

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private OfficeMapper mapper;

    @Mock
    private OfficeSummaryMapper summaryMapper;

    @InjectMocks
    private OfficeServiceImpl service;

    private UUID officeId;

    @BeforeEach
    void setUp() {
        officeId = UUID.randomUUID();
    }

    @Test
    void createShouldPersistNewOffice() {
        // Given
        OfficeCreateRequest request = new OfficeCreateRequest("Consultorio medicina", "Edificio A", "Piso 1", 101);
        Office entity = Office.builder().name("Consultorio medicina").location("Edificio A").build();
        Office saved = Office.builder().id(officeId).name("Consultorio medicina").location("Edificio A").roomNumber(101).status(OfficeStatus.AVAILABLE).createdAt(Instant.now()).build();
        OfficeResponse response = new OfficeResponse(officeId, "Consultorio medicina", "Edificio A", "Piso 1", 101, OfficeStatus.AVAILABLE, saved.getCreatedAt(), null, Collections.emptySet());

        when(mapper.toEntity(request)).thenReturn(entity);
        when(officeRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        // When
        OfficeResponse result = service.create(request);

        // Then
        assertNotNull(result);
        assertEquals(officeId, result.id());
        verify(officeRepository).save(entity);
    }

    @Test
    void findByIdShouldReturnOfficeWhenExists() {
        // Given
        Office office = Office.builder().id(officeId).name("Consultorio medicina").location("Edificio A").roomNumber(101).status(OfficeStatus.AVAILABLE).createdAt(Instant.now()).build();
        OfficeSummaryResponse response = new OfficeSummaryResponse(officeId, "Consultorio medicina", "Edificio A", 101, OfficeStatus.AVAILABLE, office.getCreatedAt(), null);

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(summaryMapper.toSummaryResponse(office)).thenReturn(response);

        // When
        OfficeSummaryResponse result = service.findById(officeId);

        // Then
        assertNotNull(result);
        assertEquals(officeId, result.id());
    }

    @Test
    void findByIdShouldThrowNotFoundWhenMissing() {
        // Given
        when(officeRepository.findById(officeId)).thenReturn(Optional.empty());

        // When - Then
        assertThrows(ResourceNotFoundException.class, () -> service.findById(officeId));
    }

    @Test
    void findAllShouldReturnAllOffices() {
        // Given
        Office office1 = Office.builder().id(officeId).name("Consultorio medicina").location("Edificio A").roomNumber(101).status(OfficeStatus.AVAILABLE).createdAt(Instant.now()).build();
        UUID officeId2 = UUID.randomUUID();
        Office office2 = Office.builder().id(officeId2).name("Consultorio pediatría").location("Edificio B").roomNumber(202).status(OfficeStatus.AVAILABLE).createdAt(Instant.now()).build();

        OfficeSummaryResponse summary1 = new OfficeSummaryResponse(officeId, "Consultorio medicina", "Edificio A", 101, OfficeStatus.AVAILABLE, office1.getCreatedAt(), null);
        OfficeSummaryResponse summary2 = new OfficeSummaryResponse(officeId2, "Consultorio pediatría", "Edificio B", 202, OfficeStatus.AVAILABLE, office2.getCreatedAt(), null);

        when(officeRepository.findAll()).thenReturn(List.of(office1, office2));
        when(summaryMapper.toSummaryResponse(office1)).thenReturn(summary1);
        when(summaryMapper.toSummaryResponse(office2)).thenReturn(summary2);

        // When
        List<OfficeSummaryResponse> result = service.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findByStatusShouldReturnOfficesByStatus() {
        // Given
        Office office = Office.builder().id(officeId).name("Consultorio medicina").location("Edificio A").roomNumber(101).status(OfficeStatus.AVAILABLE).createdAt(Instant.now()).build();
        Page<Office> page = new PageImpl<>(List.of(office));

        when(officeRepository.findByStatus(OfficeStatus.AVAILABLE, Pageable.ofSize(10))).thenReturn(page);

        // When
        Page<OfficeSummaryResponse> result = service.findByStatus(OfficeStatus.AVAILABLE, Pageable.ofSize(10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(OfficeStatus.AVAILABLE, result.getContent().get(0).status());
    }

    @Test
    void updateShouldModifyOfficeFields() {
        // Given
        Office office = Office.builder().id(officeId).name("Consultorio medicina").location("Edificio A").roomNumber(101).status(OfficeStatus.AVAILABLE).createdAt(Instant.now()).build();
        OfficeUpdateRequest request = new OfficeUpdateRequest("Consultorio renovado", "Edificio B", "Piso 2", null);
        Office updated = Office.builder().id(officeId).name("Consultorio renovado").location("Edificio B").roomNumber(101).status(OfficeStatus.AVAILABLE).createdAt(office.getCreatedAt()).updatedAt(Instant.now()).build();
        OfficeResponse response = new OfficeResponse(officeId, "Consultorio renovado", "Edificio B", "Piso 2", 101, OfficeStatus.AVAILABLE, updated.getCreatedAt(), updated.getUpdatedAt(), Collections.emptySet());

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(officeRepository.save(office)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        // When
        OfficeResponse result = service.update(officeId, request);

        // Then
        assertNotNull(result);
        assertEquals("Consultorio renovado", result.name());
        verify(officeRepository).save(office);
    }

    @Test
    void updateShouldThrowWhenOfficeNotFound() {
        // Given
        OfficeUpdateRequest request = new OfficeUpdateRequest("Consultorio renovado", "Edificio B", "Piso 2", null);
        when(officeRepository.findById(officeId)).thenReturn(Optional.empty());

        // When - Then
        assertThrows(ResourceNotFoundException.class, () -> service.update(officeId, request));
    }

    @Test
    void existsByIdAndStatusShouldReturnTrueWhenExists() {
        // Given
        when(officeRepository.existsByIdAndStatus(officeId, OfficeStatus.AVAILABLE)).thenReturn(true);

        // When
        boolean result = service.existsByIdAndStatus(officeId, OfficeStatus.AVAILABLE);

        // Then
        assertTrue(result);
    }

    @Test
    void existsByIdAndStatusShouldReturnFalseWhenNotExists() {
        // Given
        when(officeRepository.existsByIdAndStatus(officeId, OfficeStatus.AVAILABLE)).thenReturn(false);

        // When
        boolean result = service.existsByIdAndStatus(officeId, OfficeStatus.UNAVAILABLE);

        // Then
        assertFalse(result);
    }

}
