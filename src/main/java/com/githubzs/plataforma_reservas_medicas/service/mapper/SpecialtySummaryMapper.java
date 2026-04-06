package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtySummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;

@Mapper(componentModel = "spring")
public interface SpecialtySummaryMapper {

    SpecialtySummaryResponse toSummaryResponse(Specialty specialty);

}
