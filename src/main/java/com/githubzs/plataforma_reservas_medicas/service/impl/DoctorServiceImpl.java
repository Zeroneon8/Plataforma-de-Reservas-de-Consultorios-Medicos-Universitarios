package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
import com.githubzs.plataforma_reservas_medicas.service.DoctorService;
import com.githubzs.plataforma_reservas_medicas.service.mapper.DoctorMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.DoctorSummaryMapper;

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
        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id " + request.specialtyId()));

        if (doctorRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new ConflictException("A doctor with the same document number already exists");
        }
        if (doctorRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new ConflictException("A doctor with the same license number already exists");
        }
        if (doctorRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A doctor with the same email already exists");
        }

        Doctor doctor = mapper.toEntity(request);
        doctor.setSpecialty(specialty);
        doctor.setStatus(DoctorStatus.ACTIVE);
        doctor.setCreatedAt(Instant.now());
        Doctor saved = doctorRepository.save(doctor);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> findAll(Pageable pageable) {
        return doctorRepository.findAll(pageable)
                .map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorSummaryResponse findById(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .map(summaryMapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> findActiveBySpecialty(UUID specialtyId, Pageable pageable) {
        return doctorRepository.findByStatusAndSpecialty_Id(DoctorStatus.ACTIVE, specialtyId, pageable)
                .map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> findBySpecialty(UUID specialtyId, Pageable pageable) {
        return doctorRepository.findBySpecialty_Id(specialtyId, pageable)
                .map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public DoctorResponse update(UUID doctorId, DoctorUpdateRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));

        if (request.specialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.specialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id " + request.specialtyId()));
            doctor.setSpecialty(specialty);
        }

        mapper.patch(request, doctor);
        doctor.setUpdatedAt(Instant.now());
        Doctor saved = doctorRepository.save(doctor);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DoctorSummaryResponse changeStatus(UUID doctorId, DoctorStatus status) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));

        doctor.setStatus(status);
        doctor.setUpdatedAt(Instant.now());
        return summaryMapper.toSummaryResponse(doctorRepository.save(doctor));
    }

}