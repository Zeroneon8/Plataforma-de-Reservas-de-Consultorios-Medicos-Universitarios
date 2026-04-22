package com.githubzs.plataforma_reservas_medicas.services;


import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;

public interface DoctorService {
    
    DoctorResponse create(DoctorCreateRequest request);

    Page<DoctorSummaryResponse> findAll(Pageable pageable);

    DoctorSummaryResponse findById(UUID doctorId); 

    Page<DoctorSummaryResponse> findActiveBySpecialty(UUID specialtyId, Pageable pageable);

    Page<DoctorSummaryResponse> findBySpecialty(UUID specialtyId, Pageable pageable); 

    DoctorSummaryResponse update(UUID doctorId, DoctorUpdateRequest request); 

    DoctorSummaryResponse changeStatus(UUID doctorId, DoctorStatus status); 

}
