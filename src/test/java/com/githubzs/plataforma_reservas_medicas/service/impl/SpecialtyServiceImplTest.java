package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtySummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.mapper.SpecialtyMapperImpl;
import com.githubzs.plataforma_reservas_medicas.service.mapper.SpecialtySummaryMapperImpl;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceImplTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private SpecialtyServiceImpl service;

    @BeforeEach
    // Setea mappers reales para probar mapeos genuinos de MapStruct.
    void setUp() {
        var mapperImpl = new SpecialtyMapperImpl();
        var summaryMapperImpl = new SpecialtySummaryMapperImpl();

        setField(service, "mapper", mapperImpl);
        setField(service, "summaryMapper", summaryMapperImpl);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
            return;
        } catch (NoSuchFieldException e) {
            Class<?> cls = target.getClass().getSuperclass();
            while (cls != null) {
                try {
                    Field f = cls.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                } catch (NoSuchFieldException ex) {
                    cls = cls.getSuperclass();
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateSpecialtyWhenAllValidationsPass() {
        UUID specialtyId = UUID.randomUUID();
        SpecialtyCreateRequest request = new SpecialtyCreateRequest("  Cardiology  ", "  Heart diseases  ");

        when(specialtyRepository.existsByName("Cardiology")).thenReturn(false);
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(inv -> {
            Specialty specialty = inv.getArgument(0);
            specialty.setId(specialtyId);
            return specialty;
        });

        SpecialtyResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(specialtyId, result.id());
        assertEquals("Cardiology", result.name());
        assertEquals("Heart diseases", result.description());
        verify(specialtyRepository).save(any(Specialty.class));
    }

    @Test
    void shouldThrowNPEWhenRequestIsNullForCreate() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    @Test
    void shouldThrowConflictWhenNameAlreadyExistsForCreate() {
        SpecialtyCreateRequest request = new SpecialtyCreateRequest("Cardiology", "Heart diseases");

        when(specialtyRepository.existsByName("Cardiology")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void shouldReturnSpecialtyWhenNameExistsForFindByName() {
        UUID specialtyId = UUID.randomUUID();
        Specialty specialty = Specialty.builder()
                .id(specialtyId)
                .name("Cardiology")
                .description("Heart diseases")
                .build();

        when(specialtyRepository.findByName("Cardiology")).thenReturn(Optional.of(specialty));

        SpecialtyResponse result = service.findByName("  Cardiology  ");

        assertNotNull(result);
        assertEquals(specialtyId, result.id());
        assertEquals("Cardiology", result.name());
        assertEquals("Heart diseases", result.description());
    }

    @Test
    void shouldThrowNPEWhenNameIsNullForFindByName() {
        assertThrows(NullPointerException.class, () -> service.findByName(null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenNameDoesNotExistForFindByName() {
        when(specialtyRepository.findByName("Cardiology")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findByName("Cardiology"));
    }

    @Test
    void shouldReturnAllSpecialtiesForFindAll() {
        Specialty specialty1 = Specialty.builder()
                .id(UUID.randomUUID())
                .name("Cardiology")
                .description("Heart diseases")
                .build();
        Specialty specialty2 = Specialty.builder()
                .id(UUID.randomUUID())
                .name("Dermatology")
                .description("Skin diseases")
                .build();

        when(specialtyRepository.findAll()).thenReturn(List.of(specialty1, specialty2));

        List<SpecialtySummaryResponse> result = service.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cardiology", result.get(0).name());
        assertEquals("Dermatology", result.get(1).name());
    }

    @Test
    void shouldReturnTrueWhenSpecialtyExistsByName() {
        when(specialtyRepository.existsByName("Cardiology")).thenReturn(true);

        boolean result = service.existsByName("  Cardiology  ");

        assertEquals(true, result);
    }

    @Test
    void shouldReturnFalseWhenSpecialtyDoesNotExistByName() {
        when(specialtyRepository.existsByName("Cardiology")).thenReturn(false);

        boolean result = service.existsByName("Cardiology");

        assertEquals(false, result);
    }

    @Test
    void shouldThrowNPEWhenNameIsNullForExistsByName() {
        assertThrows(NullPointerException.class, () -> service.existsByName(null));
    }

}
