package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;

@Mapper(componentModel = "spring")
public interface PatientSummaryMapper {

    PatientSummaryResponse toSummaryResponse(Patient patient);

}
