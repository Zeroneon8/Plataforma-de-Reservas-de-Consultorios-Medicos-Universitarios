package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
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
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentTypeMapperImpl;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentTypeSummaryMapperImpl;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class AppointmentTypeServiceImplTest {

    @Mock
    private AppointmentTypeRepository repository;
    
    @InjectMocks
    private AppointmentTypeServiceImpl service;

    private UUID typeId;

    // Metodo para setear mappers de forma manual con sus dependencias, ya que @Spy no detecta estas dependencias
    @BeforeEach
    void setUp() {
        typeId = UUID.randomUUID();
        var mapperImpl = new AppointmentTypeMapperImpl();
        var summaryMapperImpl = new AppointmentTypeSummaryMapperImpl();
        setField(service, "mapper", mapperImpl);
        setField(service, "summaryMapper", summaryMapperImpl);
    }

    // Metodo helper para setear las dependencias de los mappers manualmente
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
    void shouldCreateNewAppointmentType() {
        AppointmentTypeCreateRequest request = new AppointmentTypeCreateRequest("  Consulta  ", "  Consulta general  ", 30);

        when(repository.existsByNameIgnoreCase("Consulta")).thenReturn(false);
        when(repository.save(any(AppointmentType.class))).thenAnswer(inv -> {
            AppointmentType at = inv.getArgument(0);
            at.setId(typeId);
            return at;
        });

        AppointmentTypeResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(typeId, result.id());
        assertEquals("Consulta", result.name());
        assertEquals("Consulta general", result.description());
        assertEquals(30, result.durationMinutes());
        verify(repository).existsByNameIgnoreCase("Consulta");
        verify(repository).save(any(AppointmentType.class));
    }

    @Test
    void shouldRejectDuplicateNameForCreate() {
        AppointmentTypeCreateRequest request = new AppointmentTypeCreateRequest("Consulta", "Consulta general", 30);
        when(repository.existsByNameIgnoreCase("Consulta")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowNPEWhenRequestIsNullForCreate() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    @Test
    void shouldRejectNonPositiveDurationForCreate() {
        AppointmentTypeCreateRequest request = new AppointmentTypeCreateRequest("Consulta", "Consulta general", 0);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).existsByNameIgnoreCase(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectDurationGreaterThan480ForCreate() {
        AppointmentTypeCreateRequest request = new AppointmentTypeCreateRequest("Consulta", "Consulta general", 481);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).existsByNameIgnoreCase(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectBlankNameForCreate() {
        AppointmentTypeCreateRequest request = new AppointmentTypeCreateRequest("   ", "Consulta general", 30);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).existsByNameIgnoreCase(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFindAll() {
        AppointmentType entity = AppointmentType.builder().id(typeId).name("Consulta").description("Consulta general").durationMinutes(30).build();

        when(repository.findAll()).thenReturn(List.of(entity));

        List<AppointmentTypeSummaryResponse> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(typeId, result.get(0).id());
        assertEquals("Consulta", result.get(0).name());
        verify(repository).findAll();
    }

    @Test
    void shouldReturnEmptyWhenNoAppointmentTypesForFindAll() {
        when(repository.findAll()).thenReturn(List.of());

        List<AppointmentTypeSummaryResponse> result = service.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(repository).findAll();
    }

    @Test
    void shouldFindById() {
        AppointmentType entity = AppointmentType.builder().id(typeId).name("Consulta").description("Consulta general").durationMinutes(30).build();

        when(repository.findById(typeId)).thenReturn(Optional.of(entity));

        AppointmentTypeSummaryResponse result = service.findById(typeId);

        assertNotNull(result);
        assertEquals(typeId, result.id());
        assertEquals("Consulta", result.name());
        verify(repository).findById(typeId);
    }

    @Test
    void shouldThrowNPEWhenIdIsNullForFindById() {
        assertThrows(NullPointerException.class, () -> service.findById(null));
    }

    @Test
    void shouldThrowNotFoundWhenIdDoesNotExist() {
        when(repository.findById(typeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(typeId));
        verify(repository).findById(typeId);
    }

}
