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

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.services.mapper.SpecialtySummaryMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorSummaryMapperImpl;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private DoctorServiceImpl service;

    private UUID doctorId;
    private UUID specialtyId;

    @BeforeEach
    // Setea mappers reales de MapStruct para validar mapeos genuinos.
    void setUp() {
        doctorId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();

        var specialtySummaryMapperImpl = new SpecialtySummaryMapperImpl();
        var doctorSummaryMapperImpl = new DoctorSummaryMapperImpl();
        var doctorMapperImpl = new DoctorMapperImpl();

        setField(doctorSummaryMapperImpl, "specialtySummaryMapper", specialtySummaryMapperImpl);
        setField(doctorMapperImpl, "specialtySummaryMapper", specialtySummaryMapperImpl);

        setField(service, "mapper", doctorMapperImpl);
        setField(service, "summaryMapper", doctorSummaryMapperImpl);
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
    void shouldCreateDoctorWhenAllValidationsPass() {
        DoctorCreateRequest request = new DoctorCreateRequest(
                "  Dr. House  ",
                " HOUSE@Example.com ",
                " 111111 ",
                " 222222 ",
                specialtyId);

        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").description("General").build();

        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorRepository.existsByDocumentNumber(request.documentNumber())).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber(request.licenseNumber())).thenReturn(false);
        when(doctorRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> {
            Doctor doctor = inv.getArgument(0);
            doctor.setId(doctorId);
            return doctor;
        });

        DoctorResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(doctorId, result.id());
        assertEquals("Dr. House", result.fullName());
        assertEquals("house@example.com", result.email());
        assertEquals(DoctorStatus.ACTIVE, result.status());
        assertEquals(specialtyId, result.specialty().id());
        assertNotNull(result.createdAt());
        verify(doctorRepository).save(any(Doctor.class));
        verify(specialtyRepository).findById(specialtyId);
    }

    @Test
    void shouldThrowNPEWhenRequestIsNullForCreate() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenSpecialtyDoesNotExistForCreate() {
        DoctorCreateRequest request = new DoctorCreateRequest("Dr. House", "house@example.com", "111111", "222222", specialtyId);

        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenDocumentNumberAlreadyExistsForCreate() {
        DoctorCreateRequest request = new DoctorCreateRequest("Dr. House", "house@example.com", "111111", "222222", specialtyId);
        Specialty specialty = Specialty.builder().id(specialtyId).build();

        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorRepository.existsByDocumentNumber(request.documentNumber())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenLicenseNumberAlreadyExistsForCreate() {
        DoctorCreateRequest request = new DoctorCreateRequest("Dr. House", "house@example.com", "111111", "222222", specialtyId);
        Specialty specialty = Specialty.builder().id(specialtyId).build();

        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorRepository.existsByDocumentNumber(request.documentNumber())).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber(request.licenseNumber())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExistsForCreate() {
        DoctorCreateRequest request = new DoctorCreateRequest("Dr. House", "house@example.com", "111111", "222222", specialtyId);
        Specialty specialty = Specialty.builder().id(specialtyId).build();

        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorRepository.existsByDocumentNumber(request.documentNumber())).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber(request.licenseNumber())).thenReturn(false);
        when(doctorRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldReturnDoctorSummaryWhenDoctorExistsForFindById() {
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").description("General").build();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. House")
                .email("house@example.com")
                .documentNumber("222222")
                .licenseNumber("111111")
                .specialty(specialty)
                .status(DoctorStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        DoctorSummaryResponse result = service.findById(doctorId);

        assertNotNull(result);
        assertEquals(doctorId, result.id());
        assertEquals("Dr. House", result.fullName());
        assertEquals(specialtyId, result.specialty().id());
    }

    @Test
    void shouldThrowNPEWhenDoctorIdIsNullForFindById() {
        assertThrows(NullPointerException.class, () -> service.findById(null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenDoctorDoesNotExistForFindById() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(doctorId));
    }

    @Test
    void shouldReturnPaginatedDoctorsForFindAll() {
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").description("General").build();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. House")
                .email("house@example.com")
                .documentNumber("222222")
                .licenseNumber("111111")
                .specialty(specialty)
                .status(DoctorStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        Page<Doctor> page = new PageImpl<>(List.of(doctor));
        when(doctorRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<DoctorSummaryResponse> result = service.findAll(Pageable.ofSize(10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(doctorId, result.getContent().get(0).id());
    }

    @Test
    void shouldReturnActiveDoctorsBySpecialtyForFindActiveBySpecialty() {
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").description("General").build();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. House")
                .email("house@example.com")
                .documentNumber("222222")
                .licenseNumber("111111")
                .specialty(specialty)
                .status(DoctorStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        Page<Doctor> page = new PageImpl<>(List.of(doctor));
        Pageable pageable = Pageable.ofSize(10);

        when(specialtyRepository.existsById(specialtyId)).thenReturn(true);
        when(doctorRepository.findByStatusAndSpecialty_Id(DoctorStatus.ACTIVE, specialtyId, pageable)).thenReturn(page);

        Page<DoctorSummaryResponse> result = service.findActiveBySpecialty(specialtyId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(doctorId, result.getContent().get(0).id());
    }

    @Test
    void shouldThrowNPEWhenSpecialtyIdIsNullForFindActiveBySpecialty() {
        assertThrows(NullPointerException.class, () -> service.findActiveBySpecialty(null, Pageable.ofSize(10)));
    }

    @Test
    void shouldThrowResourceNotFoundWhenSpecialtyDoesNotExistForFindActiveBySpecialty() {
        when(specialtyRepository.existsById(specialtyId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.findActiveBySpecialty(specialtyId, Pageable.ofSize(10)));
    }

    @Test
    void shouldReturnDoctorsBySpecialtyForFindBySpecialty() {
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").description("General").build();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. House")
                .email("house@example.com")
                .documentNumber("222222")
                .licenseNumber("111111")
                .specialty(specialty)
                .status(DoctorStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        Page<Doctor> page = new PageImpl<>(List.of(doctor));
        Pageable pageable = Pageable.ofSize(10);

        when(specialtyRepository.existsById(specialtyId)).thenReturn(true);
        when(doctorRepository.findBySpecialty_Id(specialtyId, pageable)).thenReturn(page);

        Page<DoctorSummaryResponse> result = service.findBySpecialty(specialtyId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(doctorId, result.getContent().get(0).id());
    }

    @Test
    void shouldThrowResourceNotFoundWhenSpecialtyDoesNotExistForFindBySpecialty() {
        when(specialtyRepository.existsById(specialtyId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.findBySpecialty(specialtyId, Pageable.ofSize(10)));
    }

    @Test
    void shouldUpdateDoctorWhenRequestIsValid() {
        Specialty currentSpecialty = Specialty.builder().id(specialtyId).name("Medicina General").description("General").build();
        UUID newSpecialtyId = UUID.randomUUID();
        Specialty newSpecialty = Specialty.builder().id(newSpecialtyId).name("Cardiologia").description("Cardio").build();

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. House")
                .email("house@example.com")
                .documentNumber("222222")
                .licenseNumber("111111")
                .specialty(currentSpecialty)
                .status(DoctorStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        DoctorUpdateRequest request = new DoctorUpdateRequest("  Dr. Wilson  ", " WILSON@Example.com ", newSpecialtyId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findById(newSpecialtyId)).thenReturn(Optional.of(newSpecialty));
        when(doctorRepository.existsByEmailIgnoreCase("wilson@example.com")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        DoctorResponse result = service.update(doctorId, request);

        assertNotNull(result);
        assertEquals(doctorId, result.id());
        assertEquals("Dr. Wilson", result.fullName());
        assertEquals("wilson@example.com", result.email());
        assertEquals(newSpecialtyId, result.specialty().id());
        assertNotNull(result.updatedAt());
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void shouldThrowNPEWhenDoctorIdIsNullForUpdate() {
        DoctorUpdateRequest request = new DoctorUpdateRequest("Dr. Wilson", "wilson@example.com", specialtyId);

        assertThrows(NullPointerException.class, () -> service.update(null, request));
    }

    @Test
    void shouldThrowNPEWhenRequestIsNullForUpdate() {
        assertThrows(NullPointerException.class, () -> service.update(doctorId, null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenDoctorDoesNotExistForUpdate() {
        DoctorUpdateRequest request = new DoctorUpdateRequest("Dr. Wilson", "wilson@example.com", specialtyId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(doctorId, request));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundWhenSpecialtyDoesNotExistForUpdate() {
        UUID newSpecialtyId = UUID.randomUUID();
        Specialty currentSpecialty = Specialty.builder().id(specialtyId).build();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. House")
                .email("house@example.com")
                .documentNumber("222222")
                .licenseNumber("111111")
                .specialty(currentSpecialty)
                .status(DoctorStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        DoctorUpdateRequest request = new DoctorUpdateRequest("Dr. Wilson", "wilson@example.com", newSpecialtyId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findById(newSpecialtyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(doctorId, request));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenNormalizedEmailAlreadyExistsForUpdate() {
        Specialty specialty = Specialty.builder().id(specialtyId).build();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. House")
                .email("house@example.com")
                .documentNumber("222222")
                .licenseNumber("111111")
                .specialty(specialty)
                .status(DoctorStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        DoctorUpdateRequest request = new DoctorUpdateRequest("Dr. Wilson", " WILSON@Example.com ", specialtyId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorRepository.existsByEmailIgnoreCase("wilson@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.update(doctorId, request));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldChangeStatusWhenDoctorExistsForChangeStatus() {
        Specialty specialty = Specialty.builder().id(specialtyId).name("Medicina General").description("General").build();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. House")
                .email("house@example.com")
                .documentNumber("222222")
                .licenseNumber("111111")
                .specialty(specialty)
                .status(DoctorStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        DoctorSummaryResponse result = service.changeStatus(doctorId, DoctorStatus.INACTIVE);

        assertNotNull(result);
        assertEquals(doctorId, result.id());
        assertEquals(DoctorStatus.INACTIVE, result.status());
        assertNotNull(result.updatedAt());
    }

    @Test
    void shouldThrowNPEWhenDoctorIdIsNullForChangeStatus() {
        assertThrows(NullPointerException.class, () -> service.changeStatus(null, DoctorStatus.INACTIVE));
    }

    @Test
    void shouldThrowNPEWhenStatusIsNullForChangeStatus() {
        assertThrows(NullPointerException.class, () -> service.changeStatus(doctorId, null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenDoctorDoesNotExistForChangeStatus() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.changeStatus(doctorId, DoctorStatus.INACTIVE));
        verify(doctorRepository, never()).save(any());
    }

}
