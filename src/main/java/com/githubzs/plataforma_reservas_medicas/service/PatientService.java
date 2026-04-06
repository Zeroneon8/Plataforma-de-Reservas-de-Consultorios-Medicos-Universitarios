package com.githubzs.plataforma_reservas_medicas.service;

import java.util.List;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

public interface PatientService {

    PatientResponse create(PatientCreateRequest request); 

    PatientSummaryResponse findById(UUID id);

    List<PatientSummaryResponse> findAll(); 

    PatientResponse update(UUID id, PatientUpdateRequest request); 

    PatientResponse changeStatus(UUID id, PatientStatus status); 

}
