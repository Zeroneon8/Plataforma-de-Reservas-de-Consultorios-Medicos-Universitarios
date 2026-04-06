package com.githubzs.plataforma_reservas_medicas.service;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeResponse;

public interface OfficeService {

    OfficeResponse create(OfficeCreateRequest request);// Regla negocio Valida unicidad de nombre/número y persiste.
    List<OfficeSummaryResponse> findAll(); //Consulta Lista todos los consultorios.
    OfficeResponse update(UUID id, OfficeUpdateRequest request); //Regla negocio Actualiza nombre, ubicación y estado.
    Page<Office> findByStatus(OfficeStatus status, Pageable pageable);
    OfficeResponse findById(UUID officeId); //Consulta Lanza ResourceNotFoundException si no existe.
    boolean existsByIdAndStatus(UUID id, OfficeStatus status);


}
