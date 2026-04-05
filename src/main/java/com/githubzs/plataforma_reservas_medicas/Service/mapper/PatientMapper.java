package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDto.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDto.PatientDetailResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDto.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDto.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDto.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;

@Mapper(componentModel = "spring", uses = { AppointmentMapper.class })
public interface PatientMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true) // el servicio asigna ACTIVE
    @Mapping(target = "createdAt",    ignore = true) // el servicio lo asigna
    @Mapping(target = "updatedAt",    ignore = true) // el servicio lo asigna
    @Mapping(target = "appointments", ignore = true)
    Patient toEntity(PatientCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "appointments", ignore = true)
    void applyUpdate(PatientUpdateRequest request, @MappingTarget Patient patient);

    PatientResponse toResponse(Patient patient);

    PatientSummaryResponse toSummaryResponse(Patient patient);

    
    PatientDetailResponse toDetailResponse(Patient patient);
}