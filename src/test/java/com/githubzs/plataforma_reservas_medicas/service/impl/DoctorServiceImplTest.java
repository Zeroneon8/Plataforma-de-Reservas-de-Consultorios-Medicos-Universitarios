package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
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

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtySummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.mapper.DoctorMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.DoctorSummaryMapper;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private DoctorMapper mapper;

    @Mock
    private DoctorSummaryMapper summaryMapper;

    @InjectMocks
    private DoctorServiceImpl service;

    private UUID doctorId;
    private UUID specialtyId;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
    }

    @Test
    void createShouldPersistNewDoctor() {
        // Given
        DoctorCreateRequest request = new DoctorCreateRequest("Dr. House", "house@example.com", "111111", "1111111", specialtyId);
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor entity = Doctor.builder().fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).build();
        Doctor saved = Doctor.builder().id(doctorId).fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.ACTIVE).createdAt(Instant.now()).build();
        SpecialtySummaryResponse specialtySummary = new SpecialtySummaryResponse(specialtyId, "Medicina General", "Medicina General");
        DoctorResponse response = new DoctorResponse(doctorId, "Dr. House", "house@example.com", specialtySummary, DoctorStatus.ACTIVE, saved.getCreatedAt(), null, Collections.emptySet(), Collections.emptySet());

        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(mapper.toEntity(request)).thenReturn(entity);
        when(doctorRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        // When
        DoctorResponse result = service.create(request);

        // Then
        assertNotNull(result);
        assertEquals(doctorId, result.id());
        verify(doctorRepository).save(entity);
    }

    @Test
    void createShouldThrowWhenSpecialtyNotFound() {
        // Given
        DoctorCreateRequest request = new DoctorCreateRequest("Dr. House", "house@example.com", "111111", "1111111", specialtyId);
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.empty());

        // When - Then
        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
    }

    @Test
    void findByIdShouldReturnDoctorWhenExists() {
        // Given
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor doctor = Doctor.builder().id(doctorId).fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.ACTIVE).createdAt(Instant.now()).build();
        SpecialtySummaryResponse specialtySummary = new SpecialtySummaryResponse(specialtyId, "Medicina General", "Medicina General");
        DoctorResponse response = new DoctorResponse(doctorId, "Dr. House", "house@example.com", specialtySummary, DoctorStatus.ACTIVE, doctor.getCreatedAt(), null, Collections.emptySet(), Collections.emptySet());

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(mapper.toResponse(doctor)).thenReturn(response);

        // When
        DoctorResponse result = service.findById(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(doctorId, result.id());
    }

    @Test
    void findByIdShouldThrowNotFoundWhenMissing() {
        // Given
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When - Then
        assertThrows(ResourceNotFoundException.class, () -> service.findById(doctorId));
    }

    @Test
    void findAllShouldReturnPaginatedDoctors() {
        // Given
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor doctor = Doctor.builder().id(doctorId).fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.ACTIVE).createdAt(Instant.now()).build();
        SpecialtySummaryResponse specialtySummary = new SpecialtySummaryResponse(specialtyId, "Medicina General", "Medicina General");
        DoctorSummaryResponse summary = new DoctorSummaryResponse(doctorId, "Dr. House", "house@example.com", specialtySummary, DoctorStatus.ACTIVE, doctor.getCreatedAt(), null);

        Page<Doctor> page = new PageImpl<>(java.util.List.of(doctor));

        when(doctorRepository.findAll((Pageable) org.mockito.ArgumentMatchers.any())).thenReturn(page);
        when(summaryMapper.toSummaryResponse(doctor)).thenReturn(summary);

        // When
        Page<DoctorSummaryResponse> result = service.findAll(Pageable.ofSize(10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(doctorId, result.getContent().get(0).id());
    }

    @Test
    void findActiveBySpecialtyShouldReturnActiveDoctors() {
        // Given
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor doctor = Doctor.builder().id(doctorId).fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.ACTIVE).createdAt(Instant.now()).build();
        SpecialtySummaryResponse specialtySummary = new SpecialtySummaryResponse(specialtyId, "Medicina General", "Medicina General");
        DoctorSummaryResponse summary = new DoctorSummaryResponse(doctorId, "Dr. House", "house@example.com", specialtySummary, DoctorStatus.ACTIVE, doctor.getCreatedAt(), null);

        Page<Doctor> page = new PageImpl<>(java.util.List.of(doctor));

        when(doctorRepository.findByStatusAndSpecialty_Id(DoctorStatus.ACTIVE, specialtyId, Pageable.ofSize(10))).thenReturn(page);
        when(summaryMapper.toSummaryResponse(doctor)).thenReturn(summary);

        // When
        Page<DoctorSummaryResponse> result = service.findActiveBySpecialty(specialtyId, Pageable.ofSize(10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findBySpecialtyShouldReturnDoctorsBySpecialty() {
        // Given
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor doctor = Doctor.builder().id(doctorId).fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.ACTIVE).createdAt(Instant.now()).build();
        SpecialtySummaryResponse specialtySummary = new SpecialtySummaryResponse(specialtyId, "Medicina General", "Medicina General");
        DoctorSummaryResponse summary = new DoctorSummaryResponse(doctorId, "Dr. House", "house@example.com", specialtySummary, DoctorStatus.ACTIVE, doctor.getCreatedAt(), null);

        Page<Doctor> page = new PageImpl<>(java.util.List.of(doctor));

        when(doctorRepository.findBySpecialty_Id(specialtyId, Pageable.ofSize(10))).thenReturn(page);
        when(summaryMapper.toSummaryResponse(doctor)).thenReturn(summary);

        // When
        Page<DoctorSummaryResponse> result = service.findBySpecialty(specialtyId, Pageable.ofSize(10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void updateShouldModifyDoctorFields() {
        // Given
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor doctor = Doctor.builder().id(doctorId).fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.ACTIVE).createdAt(Instant.now()).build();
        DoctorUpdateRequest request = new DoctorUpdateRequest("Dr. Wilson", "house@example.com", specialtyId);
        Doctor updated = Doctor.builder().id(doctorId).fullName("Dr. Wilson").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.ACTIVE).createdAt(doctor.getCreatedAt()).updatedAt(Instant.now()).build();
        SpecialtySummaryResponse specialtySummary = new SpecialtySummaryResponse(specialtyId, "Medicina General", "Medicina General");
        DoctorResponse response = new DoctorResponse(doctorId, "Dr. Wilson", "house@example.com", specialtySummary, DoctorStatus.ACTIVE, updated.getCreatedAt(), updated.getUpdatedAt(), Collections.emptySet(), Collections.emptySet());

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        // When
        DoctorResponse result = service.update(doctorId, request);

        // Then
        assertNotNull(result);
        assertEquals("Dr. Wilson", result.fullName());
        verify(doctorRepository).save(doctor);
    }

    @Test
    void updateShouldThrowWhenDoctorNotFound() {
        // Given
        DoctorUpdateRequest request = new DoctorUpdateRequest("Dr. Wilson", "house@example.com", specialtyId);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When - Then
        assertThrows(ResourceNotFoundException.class, () -> service.update(doctorId, request));
    }

    @Test
    void changeStatusShouldUpdateDoctorStatus() {
        // Given
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").build();
        Doctor doctor = Doctor.builder().id(doctorId).fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.ACTIVE).createdAt(Instant.now()).build();
        Doctor updated = Doctor.builder().id(doctorId).fullName("Dr. House").email("house@example.com").documentNumber("1111111").licenseNumber("111111").specialty(specialty).status(DoctorStatus.INACTIVE).createdAt(doctor.getCreatedAt()).updatedAt(Instant.now()).build();
        SpecialtySummaryResponse specialtySummary = new SpecialtySummaryResponse(specialtyId, "Medicina General", "Medicina General");
        DoctorResponse response = new DoctorResponse(doctorId, "Dr. House", "house@example.com", specialtySummary, DoctorStatus.INACTIVE, updated.getCreatedAt(), updated.getUpdatedAt(), Collections.emptySet(), Collections.emptySet());

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        // When
        DoctorResponse result = service.changeStatus(doctorId, DoctorStatus.INACTIVE);

        // Then
        assertNotNull(result);
        assertEquals(DoctorStatus.INACTIVE, result.status());
        verify(doctorRepository).save(doctor);
    }

    @Test
    void changeStatusShouldThrowWhenDoctorNotFound() {
        // Given
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When - Then
        assertThrows(ResourceNotFoundException.class, () -> service.changeStatus(doctorId, DoctorStatus.INACTIVE));
    }

}
