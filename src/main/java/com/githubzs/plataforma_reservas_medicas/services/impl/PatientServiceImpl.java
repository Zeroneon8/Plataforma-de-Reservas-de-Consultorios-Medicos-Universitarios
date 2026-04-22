package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.services.PatientService;
import com.githubzs.plataforma_reservas_medicas.services.mapper.PatientMapper;
import com.githubzs.plataforma_reservas_medicas.services.mapper.PatientSummaryMapper;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper mapper;
    private final PatientSummaryMapper summaryMapper;


    @Override
    @Transactional
    public PatientResponse create(PatientCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Patient create request is required",
                List.of(new FieldViolation("request", "is required")));
        }

        // Normalizamos nombre, email, phoneNumber, documentNumber y studentCode
        String normalizedName = request.fullName().trim();
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedPhoneNumber = request.phoneNumber().trim();
        String normalizedDocumentNumber = request.documentNumber().trim();
        String normalizedStudentCode = request.studentCode() != null ? request.studentCode().trim() : null;

        if (patientRepository.existsByDocumentNumber(normalizedDocumentNumber)) {
            throw new ConflictException("A patient with the same document number already exists");
        }
        if (patientRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("A patient with the same email already exists");
        }
        if (normalizedStudentCode != null && !normalizedStudentCode.isBlank() && patientRepository.existsByStudentCodeIgnoreCase(normalizedStudentCode)) {
            throw new ConflictException("A patient with the same student code already exists");
        }

        Patient patient = mapper.toEntity(request);
        patient.setFullName(normalizedName);
        patient.setEmail(normalizedEmail);
        patient.setPhoneNumber(normalizedPhoneNumber);
        patient.setDocumentNumber(normalizedDocumentNumber);
        if (normalizedStudentCode != null && !normalizedStudentCode.isBlank()) {
            patient.setStudentCode(normalizedStudentCode);
        }
        patient.setStatus(PatientStatus.ACTIVE);
        patient.setCreatedAt(Instant.now());
        Patient saved = patientRepository.save(patient);
        return mapper.toResponse(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public PatientSummaryResponse findById(UUID id) {
        if (id == null) {
            throw new ValidationException("Patient id is required",
                List.of(new FieldViolation("id", "is required")));
        }
        return patientRepository.findById(id)
                .map(summaryMapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientSummaryResponse> findAll(Pageable pageable) {
        Pageable finalPageable = pageable == null ? Pageable.ofSize(10) : pageable;
        return patientRepository.findAll(finalPageable)
                .map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public PatientResponse update(UUID id, PatientUpdateRequest request) {
        if (id == null) {
            throw new ValidationException("Patient id is required",
                List.of(new FieldViolation("id", "is required")));
        }
        if (request == null) {
            throw new ValidationException("Patient update request is required",
                List.of(new FieldViolation("request", "is required")));
        }

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));

        mapper.patch(request, patient);

        // Normalizamos fullName, email y phoneNumber si vienen en la request
        if (request.fullName() != null) {
            String normalizedName = request.fullName().trim();
            patient.setFullName(normalizedName);
        }
        if (request.email() != null) {
            String normalizedEmail = request.email().trim().toLowerCase();
            patient.setEmail(normalizedEmail);
        }
        if (request.phoneNumber() != null) {
            String normalizedPhoneNumber = request.phoneNumber().trim();
            patient.setPhoneNumber(normalizedPhoneNumber);
        }

        patient.setUpdatedAt(Instant.now());
        Patient saved = patientRepository.save(patient);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PatientResponse changeStatus(UUID id, PatientStatus status) {
        if (id == null) {
            throw new ValidationException("Patient id is required",
                List.of(new FieldViolation("id", "is required")));
        }
        if (status == null) {
            throw new ValidationException("Patient status is required",
                List.of(new FieldViolation("status", "is required")));
        }

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));

        patient.setStatus(status);
        patient.setUpdatedAt(Instant.now());
        Patient saved = patientRepository.save(patient);
        return mapper.toResponse(saved);
    }

}
