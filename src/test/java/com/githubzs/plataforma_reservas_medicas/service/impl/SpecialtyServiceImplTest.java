package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.mapper.SpecialtyMapper;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceImplTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private SpecialtyMapper mapper;

    @InjectMocks
    private SpecialtyServiceImpl service;

    @Test
    void createShouldSaveNewSpecialty() {
        SpecialtyCreateRequest request = new SpecialtyCreateRequest("Cardiology", "Heart diseases");
        Specialty entity = Specialty.builder().name("Cardiology").build();
        Specialty saved = Specialty.builder().id(java.util.UUID.randomUUID()).name("Cardiology").build();
        SpecialtyResponse response = new SpecialtyResponse(saved.getId(), "Cardiology", "Heart diseases", null);

        when(specialtyRepository.existsByName("Cardiology")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(specialtyRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        SpecialtyResponse result = service.create(request);

        assertNotNull(result);
        assertEquals("Cardiology", result.name());
        verify(specialtyRepository).save(entity);
    }

    @Test
    void createShouldRejectDuplicateName() {
        SpecialtyCreateRequest request = new SpecialtyCreateRequest("Cardiology", "Heart diseases");

        when(specialtyRepository.existsByName("Cardiology")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void findByNameShouldReturnResponseWhenFound() {
        Specialty specialty = Specialty.builder().id(java.util.UUID.randomUUID()).name("Cardiology").build();
        SpecialtyResponse response = new SpecialtyResponse(specialty.getId(), "Cardiology", "Heart diseases", null);

        when(specialtyRepository.findByName("Cardiology")).thenReturn(Optional.of(specialty));
        when(mapper.toResponse(specialty)).thenReturn(response);

        SpecialtyResponse result = service.findByName("Cardiology");

        assertNotNull(result);
        assertEquals("Cardiology", result.name());
    }

    @Test
    void findByNameShouldThrowNotFoundWhenMissing() {
        when(specialtyRepository.findByName("Cardiology")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findByName("Cardiology"));
    }

}