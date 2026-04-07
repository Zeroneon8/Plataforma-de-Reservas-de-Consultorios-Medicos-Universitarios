package com.githubzs.plataforma_reservas_medicas.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

public interface PatientService {

    PatientResponse create(PatientCreateRequest request); 

    PatientSummaryResponse findById(UUID id);

    Page<PatientSummaryResponse> findAll(Pageable pageable); 

    PatientResponse update(UUID id, PatientUpdateRequest request); 

    PatientResponse changeStatus(UUID id, PatientStatus status); 

}
