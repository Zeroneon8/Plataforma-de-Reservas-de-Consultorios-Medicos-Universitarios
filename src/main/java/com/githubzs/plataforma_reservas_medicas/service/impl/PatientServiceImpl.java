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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper mapper;


    @Override
    @Transactional
    public PatientResponse create(PatientCreateRequest request) {
        Objects.requireNonNull(request, "Patient create request is required");

        if (patientRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new ConflictException("A patient with the same document number already exists");
        }
        if (patientRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A patient with the same email already exists");
        }
        if (request.studentCode() != null && !request.studentCode().isBlank() && patientRepository.existsByStudentCodeIgnoreCase(request.studentCode())) {
            throw new ConflictException("A patient with the same student code already exists");
        }

        Patient patient = mapper.toEntity(request);
        patient.setStatus(PatientStatus.ACTIVE);
        patient.setCreatedAt(Instant.now());
        Patient saved = patientRepository.save(patient);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientSummaryResponse findById(UUID id) {
        Objects.requireNonNull(id, "Patient id is required");
        return patientRepository.findById(id)
                .map(mapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> findAll() {
        return patientRepository.findAll().stream()
                .map(mapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public PatientResponse update(UUID id, PatientUpdateRequest request) {
        Objects.requireNonNull(id, "Patient id is required");
        Objects.requireNonNull(request, "Patient update request is required");

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));

        mapper.patch(request, patient);
        patient.setUpdatedAt(Instant.now());
        Patient saved = patientRepository.save(patient);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PatientResponse changeStatus(UUID id, PatientStatus status) {
        Objects.requireNonNull(id, "Patient id is required");
        Objects.requireNonNull(status, "Patient status is required");
        if (status != PatientStatus.ACTIVE && status != PatientStatus.INACTIVE && status != PatientStatus.SUSPENDED) {
            throw new ConflictException("Patient status can only be changed to ACTIVE, INACTIVE or SUSPENDED");
        }

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));

        patient.setStatus(status);
        patient.setUpdatedAt(Instant.now());
        Patient saved = patientRepository.save(patient);
        return mapper.toResponse(saved);
    }


}
