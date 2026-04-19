package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.util.List;
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
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.api.error.ErrorResponse.FieldViolation;
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
        if (request == null) {
            throw new ValidationException("Appointment type request is required", 
                    List.of(new FieldViolation("request", "is required")));
        }

        int duration = request.durationMinutes();
        if (duration <= 0) {
            throw new ValidationException("Invalid duration",
                List.of(new FieldViolation("durationMinutes", "must be a positive integer")));
        }
        else if (duration > 480) {
            throw new ValidationException("Invalid duration",
                List.of(new FieldViolation("durationMinutes", "cannot exceed 480 minutes (8 hours)")));
        }

        String name = request.name();
        if (name == null || name.isBlank()) {
            throw new ValidationException("Appointment type name is required",
                List.of(new FieldViolation("name", "is required")));
        }

        String normalizedName = name.trim();
        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("An appointment type with the same name already exists");
        }

        AppointmentType appointmentType = mapper.toEntity(request);
        appointmentType.setName(normalizedName);
        appointmentType.setDurationMinutes(duration);
        String description = request.description() == null ? null : request.description().trim();
        appointmentType.setDescription(description);
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
        if (id == null) {
            throw new ValidationException("Appointment type id is required",
                List.of(new FieldViolation("id", "is required")));
        }
        return repository.findById(id)
                .map(summaryMapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found with id " + id));
    }

}
