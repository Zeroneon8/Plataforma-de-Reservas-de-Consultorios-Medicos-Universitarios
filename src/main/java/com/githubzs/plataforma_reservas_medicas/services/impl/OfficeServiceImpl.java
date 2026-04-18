package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.services.OfficeService;
import com.githubzs.plataforma_reservas_medicas.services.mapper.OfficeMapper;
import com.githubzs.plataforma_reservas_medicas.services.mapper.OfficeSummaryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;
    private final OfficeMapper mapper;
    private final OfficeSummaryMapper summaryMapper;

    @Override
    @Transactional
    public OfficeResponse create(OfficeCreateRequest request) {
        Objects.requireNonNull(request, "Office create request is required");

        // Normalizamos el nombre, la ubicación y la descripción si existe en la request
        String normalizedName = request.name().trim();
        String normalizedLocation = request.location().trim();
        String normalizedDescription = request.description() != null ? request.description().trim() : null;

        if (officeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("An office with the same name already exists");
        }

        Office office = mapper.toEntity(request);
        office.setName(normalizedName);
        office.setLocation(normalizedLocation);
        office.setDescription(normalizedDescription);
        office.setStatus(OfficeStatus.AVAILABLE);
        office.setCreatedAt(Instant.now());
        Office saved = officeRepository.save(office);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeSummaryResponse> findAll() {
        return officeRepository.findAll().stream()
                .map(summaryMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public OfficeResponse update(UUID id, OfficeUpdateRequest request) {
        Objects.requireNonNull(id, "Office id is required");
        Objects.requireNonNull(request, "Office update request is required");

        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id " + id));

        String originalName = office.getName();

        mapper.patch(request, office);

        // Normalizamos el nombre, la ubicación y la descripción si vienen en la request
        if (request.name() != null) {
            String normalizedName = request.name().trim();
            if (!normalizedName.equalsIgnoreCase(originalName) && officeRepository.existsByNameIgnoreCase(normalizedName)) {
                throw new ConflictException("An office with the same name already exists");
            }
            office.setName(normalizedName);
        }
        if (request.location() != null) {
            String normalizedLocation = request.location().trim();
            office.setLocation(normalizedLocation);
        }
        if (request.description() != null) {
            String normalizedDescription = request.description().trim();
            office.setDescription(normalizedDescription);
        }

        office.setUpdatedAt(Instant.now());
        Office saved = officeRepository.save(office);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfficeSummaryResponse> findByStatus(OfficeStatus status, Pageable pageable) {
        Objects.requireNonNull(status, "Office status is required");

        Pageable finalPageable = pageable == null ? Pageable.ofSize(10) : pageable;
        return officeRepository.findByStatus(status, finalPageable).map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OfficeSummaryResponse findById(UUID officeId) {
        Objects.requireNonNull(officeId, "Office id is required");

        return officeRepository.findById(officeId)
                .map(summaryMapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id " + officeId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndStatus(UUID id, OfficeStatus status) {
        Objects.requireNonNull(id, "Office id is required");
        Objects.requireNonNull(status, "Office status is required");

        return officeRepository.existsByIdAndStatus(id, status);
    }

}