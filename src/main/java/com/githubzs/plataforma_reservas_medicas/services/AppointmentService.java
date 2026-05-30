package com.githubzs.plataforma_reservas_medicas.services;

import java.util.UUID;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.*;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;

public interface AppointmentService {

    AppointmentResponse create(AppointmentCreateRequest request);

    AppointmentResponse findById(UUID id);

    Page<AppointmentSummaryResponse> findByDoctorDocumentNumber(String documentNumber, Pageable pageable);

    Page<AppointmentResponse> findAll(AppointmentSearchRequest request, Pageable pageable);

    AppointmentStatusUpdateResponse confirm(UUID id);

    AppointmentStatusUpdateResponse cancel(UUID id, AppointmentCancelRequest request);

    AppointmentStatusUpdateResponse complete(UUID id, AppointmentCompleteRequest request);

    AppointmentStatusUpdateResponse markNoShow(UUID id);

    long countByStatusAndDate(AppointmentStatus status, LocalDate date);
    
}