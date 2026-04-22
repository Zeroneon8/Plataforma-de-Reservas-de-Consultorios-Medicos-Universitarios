package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtySummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.SpecialtyRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.services.SpecialtyService;
import com.githubzs.plataforma_reservas_medicas.services.mapper.SpecialtyMapper;
import com.githubzs.plataforma_reservas_medicas.services.mapper.SpecialtySummaryMapper;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper mapper;
    private final SpecialtySummaryMapper summaryMapper;

    @Override
    @Transactional
    public SpecialtyResponse create(SpecialtyCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Specialty create request is required",
                List.of(new FieldViolation("request", "is required")));
        }

        // Normalizamos el nombre y la descripción (si existe) de la especialidad
        String normalizedName = request.name().trim();
        String normalizedDescription = null;
        if (request.description() != null) {
            normalizedDescription = request.description().trim();
        }

        if (specialtyRepository.existsByName(normalizedName)) {
            throw new ConflictException("A specialty with the same name already exists");
        }

        Specialty specialty = mapper.toEntity(request);
        specialty.setName(normalizedName);
        specialty.setDescription(normalizedDescription);
        Specialty saved = specialtyRepository.save(specialty);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtySummaryResponse> findAll() {
        return specialtyRepository.findAll().stream()
                .map(summaryMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialtyResponse findByName(String name) {
        if (name == null) {
            throw new ValidationException("Specialty name is required",
                List.of(new FieldViolation("name", "is required")));
        }
        String normalizedName = name.trim();
        return specialtyRepository.findByName(normalizedName)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with name " + normalizedName));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null) {
            throw new ValidationException("Specialty name is required",
                List.of(new FieldViolation("name", "is required")));
        }
        String normalizedName = name.trim();
        return specialtyRepository.existsByName(normalizedName);
    }

}