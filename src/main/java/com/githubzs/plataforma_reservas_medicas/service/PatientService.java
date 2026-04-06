package com.githubzs.plataforma_reservas_medicas.service;

import java.util.List;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

public interface PatientService {

    PatientResponse create(PatientCreateRequest request); //Regla negocio Valida unicidad (documento/email si aplica), persiste y retorna DTO.
    PatientSummaryResponse findById(UUID id);// Consulta Lanza ResourceNotFoundException si no existe.
    List<PatientSummaryResponse> findAll(); //Consulta Lista todos los pacientes.
    PatientResponse update(UUID id, PatientUpdateRequest request); //Regla negocio Actualiza datos permitidos; no modifica estado desde aquí.
    PatientResponse changeStatus(UUID id, PatientStatus status); //Transición estado Activa o inactiva el paciente.
}
