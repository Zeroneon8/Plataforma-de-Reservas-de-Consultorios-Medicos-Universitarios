package com.githubzs.plataforma_reservas_medicas.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequestDto;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;

public interface AppointmentService {

    AppointmentResponse create(AppointmentCreateRequest request);

    AppointmentResponse findById(UUID id);

    List<AppointmentSummaryResponse> findAll(UUID patientId,UUID doctorId,AppointmentStatus status,LocalDate dateFrom,LocalDate dateTo);

    AppointmentResponse confirm(UUID id);

    AppointmentResponse cancel(UUID id, AppointmentCancelRequest request);

    AppointmentResponse complete(UUID id, AppointmentCompleteRequestDto request);

    AppointmentResponse markNoShow(UUID id);
}