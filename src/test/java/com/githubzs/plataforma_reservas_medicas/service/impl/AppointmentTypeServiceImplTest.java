package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentTypeSummaryMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentTypeMapper;

@ExtendWith(MockitoExtension.class)
class AppointmentTypeServiceImplTest {

    @Mock
    private AppointmentTypeRepository repository;

    @Mock
    private AppointmentTypeMapper mapper;

    @Mock
    private AppointmentTypeSummaryMapper summaryMapper;

    @InjectMocks
    private AppointmentTypeServiceImpl service;

    private UUID typeId;

    @BeforeEach
    void setUp() {
        typeId = UUID.randomUUID();
    }

    @Test
    void createShouldSaveNewAppointmentType() {
        AppointmentTypeCreateRequest request = new AppointmentTypeCreateRequest("Consulta", "Consulta general", 30);
        AppointmentType entity = AppointmentType.builder().name("Consulta").description("Consulta general").durationMinutes(30).build();
        AppointmentType saved = AppointmentType.builder().id(typeId).name("Consulta").description("Consulta general").durationMinutes(30).build();
        AppointmentTypeResponse response = new AppointmentTypeResponse(typeId, "Consulta", "Consulta general", 30, Set.of());

        when(repository.existsByNameIgnoreCase("Consulta")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        AppointmentTypeResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(typeId, result.id());
        assertEquals("Consulta", result.name());
        verify(repository).save(entity);
    }

    @Test
    void createShouldRejectDuplicateName() {
        AppointmentTypeCreateRequest request = new AppointmentTypeCreateRequest("Consulta", "Consulta general", 30);
        when(repository.existsByNameIgnoreCase("Consulta")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void findAllShouldReturnSummaries() {
        AppointmentType entity = AppointmentType.builder().id(typeId).name("Consulta").description("Consulta general").durationMinutes(30).build();
        AppointmentTypeSummaryResponse summary = new AppointmentTypeSummaryResponse(typeId, "Consulta", "Consulta general", 30);

        when(repository.findAll()).thenReturn(List.of(entity));
        when(summaryMapper.toSummaryResponse(entity)).thenReturn(summary);

        List<AppointmentTypeSummaryResponse> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(typeId, result.get(0).id());
    }

    @Test
    void findByIdShouldReturnResponseWhenFound() {
        AppointmentType entity = AppointmentType.builder().id(typeId).name("Consulta").description("Consulta general").durationMinutes(30).build();
        AppointmentTypeResponse response = new AppointmentTypeResponse(typeId, "Consulta", "Consulta general", 30, Set.of());

        when(repository.findById(typeId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        AppointmentTypeResponse result = service.findById(typeId);

        assertNotNull(result);
        assertEquals(typeId, result.id());
    }

    @Test
    void findByIdShouldThrowNotFoundWhenMissing() {
        when(repository.findById(typeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(typeId));
    }

}
