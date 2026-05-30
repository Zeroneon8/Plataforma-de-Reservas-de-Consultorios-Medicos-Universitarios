package com.githubzs.plataforma_reservas_medicas.services;

import java.util.List;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.*;

public interface AppointmentTypeService {
    
    AppointmentTypeResponse create(AppointmentTypeCreateRequest request);

    List<AppointmentTypeSummaryResponse> findAll();
    
    AppointmentTypeSummaryResponse findById(UUID id);

}
