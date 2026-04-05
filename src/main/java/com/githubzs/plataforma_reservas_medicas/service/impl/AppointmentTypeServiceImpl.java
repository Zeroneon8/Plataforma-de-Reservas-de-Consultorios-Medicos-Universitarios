package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.AppointmentTypeService;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentTypeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentTypeServiceImpl implements AppointmentTypeService {

    private final AppointmentTypeRepository repository;
    private final AppointmentTypeMapper mapper;


    @Override
    @Transactional
    public AppointmentTypeResponse create(AppointmentTypeCreateRequest request) {
        Objects.requireNonNull(request, "Appointment type request is required");
        String name = request.name();

        if (repository.existsByNameIgnoreCase(name.trim())) {
            throw new ConflictException("An appointment type with the same name already exists");
        }

        AppointmentType appointmentType = mapper.toEntity(request);
        AppointmentType saved = repository.save(appointmentType);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentTypeSummaryResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentTypeResponse findById(UUID id) {
        Objects.requireNonNull(id, "Appointment type id is required");
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found with id " + id));
    }

}
