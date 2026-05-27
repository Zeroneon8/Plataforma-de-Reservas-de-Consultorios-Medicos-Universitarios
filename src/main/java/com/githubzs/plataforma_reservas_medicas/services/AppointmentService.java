package com.githubzs.plataforma_reservas_medicas.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSearchRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentStatusUpdateResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSummaryResponse;

public interface AppointmentService {

    AppointmentResponse create(AppointmentCreateRequest request);

    AppointmentResponse findById(UUID id);

    Page<AppointmentSummaryResponse> findByDoctorDocumentNumber(String documentNumber, Pageable pageable);

    Page<AppointmentResponse> findAll(AppointmentSearchRequest request, Pageable pageable);

    AppointmentStatusUpdateResponse confirm(UUID id);

    AppointmentStatusUpdateResponse cancel(UUID id, AppointmentCancelRequest request);

    AppointmentStatusUpdateResponse complete(UUID id, AppointmentCompleteRequest request);

    AppointmentStatusUpdateResponse markNoShow(UUID id);
    
}