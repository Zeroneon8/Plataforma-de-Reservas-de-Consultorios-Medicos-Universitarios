package com.githubzs.plataforma_reservas_medicas.services.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;

@Mapper(componentModel = "spring", uses = { AppointmentSummaryMapper.class })
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // Se setea en el servicio
    @Mapping(target = "createdAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "updatedAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "appointments", ignore = true)
    Patient toEntity(PatientCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentNumber", ignore = true)
    @Mapping(target = "studentCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    void patch(PatientUpdateRequest request, @MappingTarget Patient patient);

    PatientResponse toResponse(Patient patient);

}