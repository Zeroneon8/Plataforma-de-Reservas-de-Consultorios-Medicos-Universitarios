package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.mapper.PatientMapperImpl;
import com.githubzs.plataforma_reservas_medicas.service.mapper.PatientSummaryMapperImpl;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository repository;

    @InjectMocks
    private PatientServiceImpl service;

    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();

        var realMapper = new PatientMapperImpl();
        var realSummaryMapper = new PatientSummaryMapperImpl();

        setField(service, "mapper", realMapper);
        setField(service, "summaryMapper", realSummaryMapper);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException e) {
            Class<?> cls = target.getClass().getSuperclass();
            while (cls != null) {
                try {
                    Field field = cls.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException ignored) {
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
    void shouldCreatePatientWhenAllValidationPasses() {
        var request = new PatientCreateRequest("Juan Perez", "juan@example.com", "1234567890", "D12345", "S12345");
        when(repository.existsByDocumentNumber("D12345")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("juan@example.com")).thenReturn(false);
        when(repository.existsByStudentCodeIgnoreCase("S12345")).thenReturn(false);
        when(repository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setId(patientId);
            return patient;
        });

        PatientResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(patientId, result.id());
        assertEquals("juan@example.com", result.email());
        assertEquals(PatientStatus.ACTIVE, result.status());
        assertNotNull(result.createdAt());
        verify(repository).save(any(Patient.class));
    }

    @Test
    void shouldThrowConflictWhenDocumentNumberAlreadyExists() {
        var request = new PatientCreateRequest("Juan Perez", "juan@example.com", "1234567890", "D12345", "S12345");
        when(repository.existsByDocumentNumber("D12345")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(repository, never()).save(any(Patient.class));
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        var request = new PatientCreateRequest("Juan Perez", "juan@example.com", "1234567890", "D12345", "S12345");
        when(repository.existsByDocumentNumber("D12345")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("juan@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(repository, never()).save(any(Patient.class));
    }

    @Test
    void shouldThrowConflictWhenStudentCodeAlreadyExists() {
        var request = new PatientCreateRequest("Juan Perez", "juan@example.com", "1234567890", "D12345", "S12345");
        when(repository.existsByDocumentNumber("D12345")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("juan@example.com")).thenReturn(false);
        when(repository.existsByStudentCodeIgnoreCase("S12345")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(repository, never()).save(any(Patient.class));
    }

    @Test
    void shouldThrowNpeWhenCreateRequestIsNull() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    @Test
    void shouldFindPatientByIdWhenFound() {
        var patient = Patient.builder()
                .id(patientId)
                .fullName("Juan Perez")
                .email("juan@example.com")
                .phoneNumber("1234567890")
                .documentNumber("D12345")
                .status(PatientStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        when(repository.findById(patientId)).thenReturn(Optional.of(patient));

        PatientSummaryResponse result = service.findById(patientId);

        assertNotNull(result);
        assertEquals(patientId, result.id());
        assertEquals("Juan Perez", result.fullName());
    }

    @Test
    void shouldThrowNotFoundWhenPatientMissing() {
        when(repository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(patientId));
        verify(repository, never()).save(any(Patient.class));
    }

    @Test
    void shouldUpdatePatientAllowedFields() {
        var patient = Patient.builder()
                .id(patientId)
                .fullName("Juan Perez")
                .email("juan@example.com")
                .phoneNumber("1234567890")
                .documentNumber("D12345")
                .studentCode("S12345")
                .status(PatientStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        var request = new PatientUpdateRequest("Juan P.", "juan.p@example.com", "0987654321");
        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        when(repository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.update(patientId, request);

        assertNotNull(result);
        assertEquals("Juan P.", result.fullName());
        assertEquals("juan.p@example.com", result.email());
        assertEquals("0987654321", result.phoneNumber());
        verify(repository).save(any(Patient.class));
    }

    @Test
    void shouldChangeStatusToInactive() {
        var patient = Patient.builder()
                .id(patientId)
                .fullName("Juan Perez")
                .email("juan@example.com")
                .phoneNumber("1234567890")
                .documentNumber("D12345")
                .status(PatientStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        when(repository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.changeStatus(patientId, PatientStatus.INACTIVE);

        assertNotNull(result);
        assertEquals(PatientStatus.INACTIVE, result.status());
        verify(repository).save(any(Patient.class));
    }

    @Test
    void shouldChangeStatusToSuspended() {
        var patient = Patient.builder()
                .id(patientId)
                .fullName("Juan Perez")
                .email("juan@example.com")
                .phoneNumber("1234567890")
                .documentNumber("D12345")
                .status(PatientStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        when(repository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.changeStatus(patientId, PatientStatus.SUSPENDED);

        assertNotNull(result);
        assertEquals(PatientStatus.SUSPENDED, result.status());
        verify(repository).save(any(Patient.class));
    }
}
