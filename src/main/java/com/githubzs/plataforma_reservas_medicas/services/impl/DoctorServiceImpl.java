package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.services.DoctorService;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorMapper;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorSummaryMapper;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorMapper mapper;
    private final DoctorSummaryMapper summaryMapper;

    @Override
    @Transactional
    public DoctorResponse create(DoctorCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Doctor create request is required",
                    List.of(new FieldViolation("request", "is required"))
            );
        }
        
        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id " + request.specialtyId()));

        // Normalizamos nombre, email, licenseNumber y documentNumber
        String normalizedName = request.fullName().trim();
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedLicenseNumber = request.licenseNumber().trim();
        String normalizedDocumentNumber = request.documentNumber().trim();

        if (doctorRepository.existsByDocumentNumber(normalizedDocumentNumber)) {
            throw new ConflictException("A doctor with the same document number already exists");
        }
        if (doctorRepository.existsByLicenseNumber(normalizedLicenseNumber)) {
            throw new ConflictException("A doctor with the same license number already exists");
        }
        if (doctorRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("A doctor with the same email already exists");
        }

        Doctor doctor = mapper.toEntity(request);
        doctor.setFullName(normalizedName);
        doctor.setEmail(normalizedEmail);
        doctor.setLicenseNumber(normalizedLicenseNumber);
        doctor.setDocumentNumber(normalizedDocumentNumber);
        doctor.setSpecialty(specialty);
        doctor.setStatus(DoctorStatus.ACTIVE);
        doctor.setCreatedAt(Instant.now());
        Doctor saved = doctorRepository.save(doctor);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> findAll(Pageable pageable) {
        Pageable finalPageable = pageable == null ? Pageable.ofSize(10) : pageable;
        return doctorRepository.findAll(finalPageable)
                .map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorSummaryResponse findById(UUID doctorId) {
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }

        return doctorRepository.findById(doctorId)
                .map(summaryMapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> findActiveBySpecialty(UUID specialtyId, Pageable pageable) {
        if (specialtyId == null) {
            throw new ValidationException("Specialty id is required",
                List.of(new FieldViolation("specialtyId", "is required")));
        }

        if (!specialtyRepository.existsById(specialtyId)) {
            throw new ResourceNotFoundException("Specialty not found with id " + specialtyId);
        }

        Pageable finalPageable = pageable == null ? Pageable.ofSize(10) : pageable;
        return doctorRepository.findByStatusAndSpecialty_Id(DoctorStatus.ACTIVE, specialtyId, finalPageable)
                .map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> findBySpecialty(UUID specialtyId, Pageable pageable) {
        if (specialtyId == null) {
            throw new ValidationException("Specialty id is required",
                List.of(new FieldViolation("specialtyId", "is required")));
        }

        if (!specialtyRepository.existsById(specialtyId)) {
            throw new ResourceNotFoundException("Specialty not found with id " + specialtyId);
        }

        Pageable finalPageable = pageable == null ? Pageable.ofSize(10) : pageable;
        return doctorRepository.findBySpecialty_Id(specialtyId, finalPageable)
                .map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public DoctorResponse update(UUID doctorId, DoctorUpdateRequest request) {
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }
        if (request == null) {
            throw new ValidationException("Doctor update request is required",
                List.of(new FieldViolation("request", "is required")));
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));

        if (request.specialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.specialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id " + request.specialtyId()));
            doctor.setSpecialty(specialty);
        }

        mapper.patch(request, doctor);

        if (doctor.getFullName() != null) {
            doctor.setFullName(doctor.getFullName().trim());
        }

        if (doctor.getEmail() != null) {
            String normalizedEmail = doctor.getEmail().trim().toLowerCase();
            if (!normalizedEmail.equals(doctor.getEmail()) && doctorRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new ConflictException("A doctor with the same email already exists");
            }
            doctor.setEmail(normalizedEmail);
        }

        doctor.setUpdatedAt(Instant.now());
        Doctor saved = doctorRepository.save(doctor);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DoctorSummaryResponse changeStatus(UUID doctorId, DoctorStatus status) {
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }
        if (status == null) {
            throw new ValidationException("Doctor status is required",
                List.of(new FieldViolation("status", "is required")));
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));

        doctor.setStatus(status);
        doctor.setUpdatedAt(Instant.now());
        return summaryMapper.toSummaryResponse(doctorRepository.save(doctor));
    }

}