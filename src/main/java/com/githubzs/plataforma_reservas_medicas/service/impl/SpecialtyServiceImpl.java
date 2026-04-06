package com.githubzs.plataforma_reservas_medicas.service.impl;

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
import com.githubzs.plataforma_reservas_medicas.service.SpecialtyService;
import com.githubzs.plataforma_reservas_medicas.service.mapper.SpecialtyMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.SpecialtySummaryMapper;

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
        if (specialtyRepository.existsByName(request.name())) {
            throw new ConflictException("A specialty with the same name already exists");
        }

        Specialty specialty = mapper.toEntity(request);
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
        return specialtyRepository.findByName(name)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with name " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return specialtyRepository.existsByName(name);
    }

}