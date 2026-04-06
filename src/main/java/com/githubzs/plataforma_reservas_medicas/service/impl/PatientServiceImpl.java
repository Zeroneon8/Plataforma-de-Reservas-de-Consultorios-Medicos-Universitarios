package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
import com.githubzs.plataforma_reservas_medicas.service.PatientService;
import com.githubzs.plataforma_reservas_medicas.service.mapper.PatientMapper;

<<<<<<< HEAD
@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;
    private final PatientMapper mapper;

    public PatientServiceImpl(PatientRepository repository, PatientMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
=======
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper mapper;

>>>>>>> repository-layer

    @Override
    @Transactional
    public PatientResponse create(PatientCreateRequest request) {
        Objects.requireNonNull(request, "Patient create request is required");
<<<<<<< HEAD
        validateRequest(request);

        if (repository.existsByDocumentNumber(request.documentNumber())) {
            throw new ConflictException("A patient with the same document number already exists");
        }
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A patient with the same email already exists");
        }
        if (request.studentCode() != null && !request.studentCode().isBlank() && repository.existsByStudentCodeIgnoreCase(request.studentCode())) {
=======

        if (patientRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new ConflictException("A patient with the same document number already exists");
        }
        if (patientRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A patient with the same email already exists");
        }
        if (request.studentCode() != null && !request.studentCode().isBlank() && patientRepository.existsByStudentCodeIgnoreCase(request.studentCode())) {
>>>>>>> repository-layer
            throw new ConflictException("A patient with the same student code already exists");
        }

        Patient patient = mapper.toEntity(request);
        patient.setStatus(PatientStatus.ACTIVE);
        patient.setCreatedAt(Instant.now());
<<<<<<< HEAD
        Patient saved = repository.save(patient);
=======
        Patient saved = patientRepository.save(patient);
>>>>>>> repository-layer
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse findById(UUID id) {
        Objects.requireNonNull(id, "Patient id is required");
<<<<<<< HEAD
        return repository.findById(id)
=======
        return patientRepository.findById(id)
>>>>>>> repository-layer
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> findAll() {
<<<<<<< HEAD
        return repository.findAll().stream()
=======
        return patientRepository.findAll().stream()
>>>>>>> repository-layer
                .map(mapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public PatientResponse update(UUID id, PatientUpdateRequest request) {
        Objects.requireNonNull(id, "Patient id is required");
        Objects.requireNonNull(request, "Patient update request is required");

<<<<<<< HEAD
        Patient patient = repository.findById(id)
=======
        Patient patient = patientRepository.findById(id)
>>>>>>> repository-layer
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));

        mapper.patch(request, patient);
        patient.setUpdatedAt(Instant.now());
<<<<<<< HEAD
        Patient saved = repository.save(patient);
=======
        Patient saved = patientRepository.save(patient);
>>>>>>> repository-layer
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PatientResponse changeStatus(UUID id, PatientStatus status) {
        Objects.requireNonNull(id, "Patient id is required");
        Objects.requireNonNull(status, "Patient status is required");
        if (status != PatientStatus.ACTIVE && status != PatientStatus.INACTIVE) {
            throw new ConflictException("Patient status can only be changed to ACTIVE or INACTIVE");
        }

<<<<<<< HEAD
        Patient patient = repository.findById(id)
=======
        Patient patient = patientRepository.findById(id)
>>>>>>> repository-layer
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));

        patient.setStatus(status);
        patient.setUpdatedAt(Instant.now());
<<<<<<< HEAD
        Patient saved = repository.save(patient);
        return mapper.toResponse(saved);
    }

    private void validateRequest(PatientCreateRequest request) {
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new IllegalArgumentException("Patient full name is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Patient email is required");
        }
        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            throw new IllegalArgumentException("Patient phone number is required");
        }
        if (request.documentNumber() == null || request.documentNumber().isBlank()) {
            throw new IllegalArgumentException("Patient document number is required");
        }
    }
=======
        Patient saved = patientRepository.save(patient);
        return mapper.toResponse(saved);
    }

>>>>>>> repository-layer

}
