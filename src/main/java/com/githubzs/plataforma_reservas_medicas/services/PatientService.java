package com.githubzs.plataforma_reservas_medicas.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.*;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

public interface PatientService {

    PatientResponse create(PatientCreateRequest request); 

    PatientSummaryResponse findById(UUID id);

    Page<PatientSummaryResponse> findAll(Pageable pageable); 

    PatientResponse update(UUID id, PatientUpdateRequest request); 

    PatientResponse changeStatus(UUID id, PatientStatus status); 

    long countByStatus(PatientStatus status);

}
