package com.githubzs.plataforma_reservas_medicas.service;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeResponse;

public interface OfficeService {

    OfficeResponse create(OfficeCreateRequest request);

    List<OfficeSummaryResponse> findAll();

    OfficeResponse update(UUID id, OfficeUpdateRequest request);
    
    Page<OfficeSummaryResponse> findByStatus(OfficeStatus status, Pageable pageable);

    OfficeSummaryResponse findById(UUID officeId); 

    boolean existsByIdAndStatus(UUID id, OfficeStatus status);

}
