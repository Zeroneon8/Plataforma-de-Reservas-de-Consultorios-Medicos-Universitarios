package com.githubzs.plataforma_reservas_medicas.Service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.Api.dto.PatientDto.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.Api.dto.PatientDto.PatientDetailResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.PatientDto.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.PatientDto.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;

@Mapper(componentModel = "spring", uses = { AppointmentMapper.class })
public interface PatientMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true) // el servicio asigna ACTIVE
    @Mapping(target = "createdAt",    ignore = true) // el servicio lo asigna
    @Mapping(target = "updatedAt",    ignore = true) // el servicio lo asigna
    @Mapping(target = "appointments", ignore = true)
    Patient toEntity(PatientCreateRequest request);

    PatientResponse toResponse(Patient patient);

    PatientSummaryResponse toSummaryResponse(Patient patient);

    
    PatientDetailResponse toDetailResponse(Patient patient);
}