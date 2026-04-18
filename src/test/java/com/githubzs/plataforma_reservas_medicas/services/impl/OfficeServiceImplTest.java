package com.githubzs.plataforma_reservas_medicas.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
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
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.services.mapper.OfficeMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.OfficeSummaryMapperImpl;

@ExtendWith(MockitoExtension.class)
class OfficeServiceImplTest {

    @Mock
    private OfficeRepository officeRepository;

    @InjectMocks
    private OfficeServiceImpl service;

    private UUID officeId;

    @BeforeEach
    // Setea mappers reales de MapStruct para validar mapeos genuinos.
    void setUp() {
        officeId = UUID.randomUUID();

        var mapperImpl = new OfficeMapperImpl();
        var summaryMapperImpl = new OfficeSummaryMapperImpl();

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
    void shouldCreateOfficeWhenAllValidationsPass() {
        OfficeCreateRequest request = new OfficeCreateRequest(
                "  Consultorio Medicina  ",
                "  Edificio A  ",
                "  Piso 1  ",
                101);

        when(officeRepository.existsByNameIgnoreCase("Consultorio Medicina")).thenReturn(false);
        when(officeRepository.save(any(Office.class))).thenAnswer(inv -> {
            Office office = inv.getArgument(0);
            office.setId(officeId);
            return office;
        });

        OfficeResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(officeId, result.id());
        assertEquals("Consultorio Medicina", result.name());
        assertEquals("Edificio A", result.location());
        assertEquals("Piso 1", result.description());
        assertEquals(101, result.roomNumber());
        assertEquals(OfficeStatus.AVAILABLE, result.status());
        assertNotNull(result.createdAt());
        verify(officeRepository).save(any(Office.class));
    }

    @Test
    void shouldThrowNPEWhenRequestIsNullForCreate() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    @Test
    void shouldThrowConflictWhenOfficeNameAlreadyExistsForCreate() {
        OfficeCreateRequest request = new OfficeCreateRequest("Consultorio Medicina", "Edificio A", "Piso 1", 101);

        when(officeRepository.existsByNameIgnoreCase("Consultorio Medicina")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(officeRepository, never()).save(any());
    }

    @Test
    void shouldReturnOfficeSummaryWhenOfficeExistsForFindById() {
        Office office = Office.builder()
                .id(officeId)
                .name("Consultorio Medicina")
                .location("Edificio A")
                .description("Piso 1")
                .roomNumber(101)
                .status(OfficeStatus.AVAILABLE)
                .createdAt(Instant.now())
                .build();

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));

        OfficeSummaryResponse result = service.findById(officeId);

        assertNotNull(result);
        assertEquals(officeId, result.id());
        assertEquals("Consultorio Medicina", result.name());
        assertEquals(OfficeStatus.AVAILABLE, result.status());
    }

    @Test
    void shouldThrowNPEWhenIdIsNullForFindById() {
        assertThrows(NullPointerException.class, () -> service.findById(null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenOfficeDoesNotExistForFindById() {
        when(officeRepository.findById(officeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(officeId));
    }

    @Test
    void shouldReturnAllOfficesForFindAll() {
        Office office1 = Office.builder()
                .id(officeId)
                .name("Consultorio Medicina")
                .location("Edificio A")
                .roomNumber(101)
                .status(OfficeStatus.AVAILABLE)
                .createdAt(Instant.now())
                .build();
        UUID officeId2 = UUID.randomUUID();
        Office office2 = Office.builder()
                .id(officeId2)
                .name("Consultorio Pediatria")
                .location("Edificio B")
                .roomNumber(202)
                .status(OfficeStatus.UNAVAILABLE)
                .createdAt(Instant.now())
                .build();

        when(officeRepository.findAll()).thenReturn(List.of(office1, office2));

        List<OfficeSummaryResponse> result = service.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(officeId, result.get(0).id());
        assertEquals(officeId2, result.get(1).id());
    }

    @Test
    void shouldReturnOfficesByStatusForFindByStatus() {
        Office office = Office.builder()
                .id(officeId)
                .name("Consultorio Medicina")
                .location("Edificio A")
                .roomNumber(101)
                .status(OfficeStatus.AVAILABLE)
                .createdAt(Instant.now())
                .build();

        Pageable pageable = Pageable.ofSize(10);
        Page<Office> page = new PageImpl<>(List.of(office));

        when(officeRepository.findByStatus(OfficeStatus.AVAILABLE, pageable)).thenReturn(page);

        Page<OfficeSummaryResponse> result = service.findByStatus(OfficeStatus.AVAILABLE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(OfficeStatus.AVAILABLE, result.getContent().get(0).status());
    }

    @Test
    void shouldThrowNPEWhenStatusIsNullForFindByStatus() {
        assertThrows(NullPointerException.class, () -> service.findByStatus(null, Pageable.ofSize(10)));
    }

    @Test
    void shouldUseDefaultPageableWhenNullForFindByStatus() {
        Office office = Office.builder()
                .id(officeId)
                .name("Consultorio Medicina")
                .location("Edificio A")
                .roomNumber(101)
                .status(OfficeStatus.AVAILABLE)
                .createdAt(Instant.now())
                .build();

        when(officeRepository.findByStatus(any(OfficeStatus.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(office)));

        Page<OfficeSummaryResponse> result = service.findByStatus(OfficeStatus.AVAILABLE, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldUpdateOfficeWhenRequestIsValid() {
        Office office = Office.builder()
                .id(officeId)
                .name("Consultorio Medicina")
                .location("Edificio A")
                .description("Piso 1")
                .roomNumber(101)
                .status(OfficeStatus.AVAILABLE)
                .createdAt(Instant.now())
                .build();

        OfficeUpdateRequest request = new OfficeUpdateRequest("  Consultorio Renovado  ", "  Edificio B  ", "  Piso 2  ", null);

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(officeRepository.existsByNameIgnoreCase("Consultorio Renovado")).thenReturn(false);
        when(officeRepository.save(any(Office.class))).thenAnswer(inv -> inv.getArgument(0));

        OfficeResponse result = service.update(officeId, request);

        assertNotNull(result);
        assertEquals(officeId, result.id());
        assertEquals("Consultorio Renovado", result.name());
        assertEquals("Edificio B", result.location());
        assertEquals("Piso 2", result.description());
        assertNotNull(result.updatedAt());
        verify(officeRepository).save(any(Office.class));
    }

    @Test
    void shouldThrowNPEWhenIdIsNullForUpdate() {
        OfficeUpdateRequest request = new OfficeUpdateRequest("Consultorio Renovado", "Edificio B", "Piso 2", 102);

        assertThrows(NullPointerException.class, () -> service.update(null, request));
    }

    @Test
    void shouldThrowNPEWhenRequestIsNullForUpdate() {
        assertThrows(NullPointerException.class, () -> service.update(officeId, null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenOfficeDoesNotExistForUpdate() {
        OfficeUpdateRequest request = new OfficeUpdateRequest("Consultorio Renovado", "Edificio B", "Piso 2", 102);

        when(officeRepository.findById(officeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(officeId, request));
        verify(officeRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenNameAlreadyExistsForUpdate() {
        Office office = Office.builder()
                .id(officeId)
                .name("Consultorio Medicina")
                .location("Edificio A")
                .description("Piso 1")
                .roomNumber(101)
                .status(OfficeStatus.AVAILABLE)
                .createdAt(Instant.now())
                .build();

        OfficeUpdateRequest request = new OfficeUpdateRequest("Consultorio Renovado", "Edificio B", "Piso 2", 102);

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(officeRepository.existsByNameIgnoreCase("Consultorio Renovado")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.update(officeId, request));
        verify(officeRepository, never()).save(any());
    }

    @Test
    void shouldReturnTrueWhenOfficeExistsByIdAndStatus() {
        when(officeRepository.existsByIdAndStatus(officeId, OfficeStatus.AVAILABLE)).thenReturn(true);

        boolean result = service.existsByIdAndStatus(officeId, OfficeStatus.AVAILABLE);

        assertEquals(true, result);
    }

    @Test
    void shouldReturnFalseWhenOfficeDoesNotExistByIdAndStatus() {
        when(officeRepository.existsByIdAndStatus(officeId, OfficeStatus.UNAVAILABLE)).thenReturn(false);

        boolean result = service.existsByIdAndStatus(officeId, OfficeStatus.UNAVAILABLE);

        assertEquals(false, result);
    }

    @Test
    void shouldThrowNPEWhenIdIsNullForExistsByIdAndStatus() {
        assertThrows(NullPointerException.class, () -> service.existsByIdAndStatus(null, OfficeStatus.AVAILABLE));
    }

    @Test
    void shouldThrowNPEWhenStatusIsNullForExistsByIdAndStatus() {
        assertThrows(NullPointerException.class, () -> service.existsByIdAndStatus(officeId, null));
    }

}
