package com.githubzs.plataforma_reservas_medicas.services.impl;

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
import com.githubzs.plataforma_reservas_medicas.services.AppointmentTypeService;
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentTypeMapper;
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentTypeSummaryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentTypeServiceImpl implements AppointmentTypeService {

    private final AppointmentTypeRepository repository;
    private final AppointmentTypeMapper mapper;
    private final AppointmentTypeSummaryMapper summaryMapper;

    @Override
    @Transactional
    public AppointmentTypeResponse create(AppointmentTypeCreateRequest request) {
        Objects.requireNonNull(request, "Appointment type request is required");

        int duration = request.durationMinutes();
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be a positive integer");
        }
        else if (duration > 480) {
            throw new IllegalArgumentException("Duration cannot exceed 480 minutes (8 hours)");
        }

        String name = request.name();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Appointment type name is required");
        }

        String normalizedName = name.trim();
        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("An appointment type with the same name already exists");
        }

        AppointmentType appointmentType = mapper.toEntity(request);
        appointmentType.setName(normalizedName);
        appointmentType.setDurationMinutes(duration);
        appointmentType.setDescription(request.description().trim());
        AppointmentType saved = repository.save(appointmentType);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentTypeSummaryResponse> findAll() {
        return repository.findAll().stream()
                .map(summaryMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentTypeSummaryResponse findById(UUID id) {
        Objects.requireNonNull(id, "Appointment type id is required");
        return repository.findById(id)
                .map(summaryMapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found with id " + id));
    }

}
