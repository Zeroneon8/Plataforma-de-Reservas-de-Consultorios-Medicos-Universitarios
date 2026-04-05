package com.githubzs.plataforma_reservas_medicas.service;

import java.util.List;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeSummaryResponse;



public interface AppointmentTypeService {
    
    AppointmentTypeResponse create(AppointmentTypeCreateRequest request);
    List<AppointmentTypeSummaryResponse> findAll();
    AppointmentTypeResponse findById(UUID id);

}
