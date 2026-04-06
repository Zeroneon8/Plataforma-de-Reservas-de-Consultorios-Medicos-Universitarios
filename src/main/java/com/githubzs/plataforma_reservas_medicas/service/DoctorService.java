package com.githubzs.plataforma_reservas_medicas.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorUpdateRequest;

public interface DoctorService {
    
    DoctorResponse create(DoctorCreateRequest request); //Regla negocio Valida que la especialidad exista y persiste el doctor activo.
    Page<DoctorSummaryResponse> findAll(Pageable pageable); //Consulta Lista todos los doctores.
    Page<DoctorSummaryResponse> findActiveBySpecialty(UUID specialtyId, Pageable pageable);
    Page<DoctorSummaryResponse> findBySpecialty(UUID specialtyId, Pageable pageable); //Consulta Delega al repository query method de doctores activos por especialidad.
    DoctorResponse update(UUID id, DoctorUpdateRequest request); //Regla negocio Actualiza datos; si cambia especialidad valida que exista.
    DoctorResponse changeStatus(UUID id, boolean active); //Transición estado Activa o inactiva el doctor.

}
