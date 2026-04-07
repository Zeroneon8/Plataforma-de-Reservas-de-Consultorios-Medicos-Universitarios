package com.githubzs.plataforma_reservas_medicas.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSearchRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSummaryResponse;

public interface AppointmentService {

    AppointmentResponse create(AppointmentCreateRequest request);

    AppointmentResponse findById(UUID id);

    Page<AppointmentSummaryResponse> findAll(AppointmentSearchRequest request, Pageable pageable);

    AppointmentSummaryResponse confirm(UUID id);

    AppointmentSummaryResponse cancel(UUID id, AppointmentCancelRequest request);

    AppointmentSummaryResponse complete(UUID id, AppointmentCompleteRequest request);

    AppointmentSummaryResponse markNoShow(UUID id);
    
}