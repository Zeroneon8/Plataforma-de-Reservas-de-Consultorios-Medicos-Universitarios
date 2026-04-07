package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.mapper.PatientMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.PatientSummaryMapper;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository repository;

    @Mock
    private PatientMapper mapper;

    @Mock
    private PatientSummaryMapper summaryMapper;

    @InjectMocks
    private PatientServiceImpl service;

    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
    }

    @Test
    void createShouldPersistNewPatient() {
        PatientCreateRequest request = new PatientCreateRequest("Juan Perez", "juan@example.com", "1234567890", "D12345", "S12345");
        Patient entity = Patient.builder().fullName("Juan Perez").email("juan@example.com").phoneNumber("1234567890").documentNumber("D12345").studentCode("S12345").build();
        Patient saved = Patient.builder().id(patientId).fullName("Juan Perez").email("juan@example.com").phoneNumber("1234567890").documentNumber("D12345").studentCode("S12345").status(PatientStatus.ACTIVE).createdAt(Instant.now()).build();
        PatientResponse response = new PatientResponse(patientId, "Juan Perez", "juan@example.com", "1234567890", PatientStatus.ACTIVE, saved.getCreatedAt(), null, null);

        when(repository.existsByDocumentNumber("D12345")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("juan@example.com")).thenReturn(false);
        when(repository.existsByStudentCodeIgnoreCase("S12345")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        PatientResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(patientId, result.id());
        verify(repository).save(entity);
    }

    @Test
    void createShouldRejectDuplicateDocumentNumber() {
        PatientCreateRequest request = new PatientCreateRequest("Juan Perez", "juan@example.com", "1234567890", "D12345", "S12345");
        when(repository.existsByDocumentNumber("D12345")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void findByIdShouldReturnPatientWhenExists() {
        Patient patient = Patient.builder().id(patientId).fullName("Juan Perez").email("juan@example.com").phoneNumber("1234567890").documentNumber("D12345").status(PatientStatus.ACTIVE).createdAt(Instant.now()).build();
        PatientSummaryResponse summary = new PatientSummaryResponse(patientId, "Juan Perez", "juan@example.com", "1234567890", PatientStatus.ACTIVE, patient.getCreatedAt(), null);

        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        when(summaryMapper.toSummaryResponse(patient)).thenReturn(summary);

        PatientSummaryResponse result = service.findById(patientId);

        assertNotNull(result);
        assertEquals(patientId, result.id());
    }

    @Test
    void findByIdShouldThrowNotFoundWhenMissing() {
        when(repository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(patientId));
    }

    /* 
    @Test
    void findAllShouldReturnSummaries() {
        Patient patient = Patient.builder().id(patientId).fullName("Juan Perez").email("juan@example.com").phoneNumber("1234567890").documentNumber("D12345").status(PatientStatus.ACTIVE).createdAt(Instant.now()).build();
        PatientSummaryResponse summary = new PatientSummaryResponse(patientId, "Juan Perez", "juan@example.com", "1234567890", PatientStatus.ACTIVE, patient.getCreatedAt(), null);

        when(repository.findAll()).thenReturn(List.of(patient));
        when(summaryMapper.toSummaryResponse(patient)).thenReturn(summary);

        List<PatientSummaryResponse> results = service.findAll();

        assertEquals(1, results.size());
        assertEquals(patientId, results.get(0).id());
    }
    */

    @Test
    void updateShouldPatchAllowedFields() {
        Patient patient = Patient.builder().id(patientId).fullName("Juan Perez").email("juan@example.com").phoneNumber("1234567890").documentNumber("D12345").studentCode("S12345").status(PatientStatus.ACTIVE).createdAt(Instant.now()).build();
        PatientUpdateRequest request = new PatientUpdateRequest("Juan P.", "juan.p@example.com", "0987654321");
        Patient updated = Patient.builder().id(patientId).fullName("Juan P.").email("juan.p@example.com").phoneNumber("0987654321").documentNumber("D12345").studentCode("S12345").status(PatientStatus.ACTIVE).createdAt(patient.getCreatedAt()).updatedAt(Instant.now()).build();
        PatientResponse response = new PatientResponse(patientId, "Juan P.", "juan.p@example.com", "0987654321", PatientStatus.ACTIVE, patient.getCreatedAt(), updated.getUpdatedAt(), null);

        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        when(mapper.toResponse(updated)).thenReturn(response);
        when(repository.save(patient)).thenReturn(updated);

        PatientResponse result = service.update(patientId, request);

        assertNotNull(result);
        assertEquals("Juan P.", result.fullName());
        verify(repository).save(patient);
    }

    @Test
    void changeStatusShouldSetInactive() {
        Patient patient = Patient.builder().id(patientId).fullName("Juan Perez").email("juan@example.com").phoneNumber("1234567890").documentNumber("D12345").status(PatientStatus.ACTIVE).createdAt(Instant.now()).build();
        Patient updated = Patient.builder().id(patientId).fullName("Juan Perez").email("juan@example.com").phoneNumber("1234567890").documentNumber("D12345").status(PatientStatus.INACTIVE).createdAt(patient.getCreatedAt()).updatedAt(Instant.now()).build();
        PatientResponse response = new PatientResponse(patientId, "Juan Perez", "juan@example.com", "1234567890", PatientStatus.INACTIVE, patient.getCreatedAt(), updated.getUpdatedAt(), null);

        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        when(repository.save(patient)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        PatientResponse result = service.changeStatus(patientId, PatientStatus.INACTIVE);

        assertNotNull(result);
        assertEquals(PatientStatus.INACTIVE, result.status());
        verify(repository).save(patient);
    }

    @Test
    void changeStatusShouldRejectUnsupportedStatus() {
        assertThrows(ConflictException.class, () -> service.changeStatus(patientId, PatientStatus.SUSPENDED));
    }

}
