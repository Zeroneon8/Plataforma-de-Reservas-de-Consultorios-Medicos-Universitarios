package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
import com.githubzs.plataforma_reservas_medicas.service.OfficeService;
import com.githubzs.plataforma_reservas_medicas.service.mapper.OfficeMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.OfficeSummaryMapper;

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
        if (officeRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("An office with the same name already exists");
        }
        /*if (officeRepository.existsByRoomNumber(request.roomNumber())) {
            throw new ConflictException("An office with the same room number already exists");
        }*/ //LA CAGUE EN ESTA MONDAAAA PORQUE PUSE EL METODO COMO STRING Y ERA COMO INT. HAY QUE CORREGIR EN CAPA REPOSITORY.

        Office office = mapper.toEntity(request);
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
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id " + id));

        mapper.patch(request, office);
        office.setUpdatedAt(Instant.now());
        Office saved = officeRepository.save(office);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Office> findByStatus(OfficeStatus status, Pageable pageable) {
        return officeRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public OfficeResponse findById(UUID officeId) {
        return officeRepository.findById(officeId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id " + officeId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndStatus(UUID id, OfficeStatus status) {
        return officeRepository.existsByIdAndStatus(id, status);
    }

}